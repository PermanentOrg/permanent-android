package org.permanent.permanent.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.R
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.network.StelaAuthState
import org.permanent.permanent.network.models.IFolderChildrenListener
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.myFiles.SortType
import java.util.*

class PublicFolderViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val TAG = PublicFolderViewModel::class.java.simpleName
    private var existsRecords = MutableLiveData(false)
    private val onFolderNameChanged = SingleLiveEvent<String>()
    private val isBusy = MutableLiveData(false)
    private val showMessage = SingleLiveEvent<String>()
    private var folderPathStack: Stack<Record> = Stack()
    private val onRecordsRetrieved = SingleLiveEvent<MutableList<Record>>()
    private val onFileViewRequest = SingleLiveEvent<ArrayList<Record>>()
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)

    fun setRootFolder(rootFolder: Record?) {
        rootFolder?.let {
            folderPathStack.push(it)
            loadFilesOf(it)
        }
    }

    fun onRecordClick(record: Record) {
        if (record.type == RecordType.FOLDER) {
            folderPathStack.push(record)
            loadFilesOf(record)
        } else {
            record.displayFirstInCarousel = true
            onFileViewRequest.value = getFilesForViewing(onRecordsRetrieved.value)
        }
    }

    private fun getFilesForViewing(allRecords: List<Record>?): ArrayList<Record> {
        val files = ArrayList<Record>()
        allRecords?.let {
            for (record in it) {
                if (record.type == RecordType.FILE) files.add(record)
            }
        }
        return files
    }

    private fun loadFilesOf(record: Record) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        val archiveNr = record.archiveNr
        val folderLinkId = record.folderLinkId
        if (archiveNr != null && folderLinkId != null) {
            // Public Gallery navigation (VSP-1810) takes the Stela V2 children endpoint
            // when the migration flag is on, with V1 as an automatic failsafe. A
            // deep-linked folder synthesized without a folderId falls through to V1 here.
            val folderId = record.folderId
            if (StelaAuthState.isV2ReadEnabled && folderId != null && folderId > 0) {
                loadFilesOfV2(record, folderId, archiveNr, folderLinkId)
            } else {
                loadFilesOfV1(archiveNr, folderLinkId)
            }
        } else {
            // The folder can't be listed without its V1 address — report it instead
            // of silently ignoring the navigation.
            if (BuildConfig.DEBUG) Log.w(
                TAG,
                "Navigation dropped: record missing V1 ids (archiveNr=$archiveNr, folderLinkId=$folderLinkId)"
            )
            showMessage.value = getApplication<Application>().getString(R.string.generic_error)
        }
    }

    private fun loadFilesOfV1(archiveNr: String, folderLinkId: Int) {
        isBusy.value = true
        fileRepository.getChildRecordsOf(archiveNr,
            folderLinkId,
            SortType.NAME_ASCENDING?.toBackendString(),
            object : IFileRepository.IOnRecordsRetrievedListener {

                override fun onSuccess(parentFolderName: String?, recordVOs: List<RecordVO>?) {
                    isBusy.value = false
                    onFolderNameChanged.value = parentFolderName
                    existsRecords.value = !recordVOs.isNullOrEmpty()
                    recordVOs?.let {
                        onRecordsRetrieved.value = getRecords(recordVOs, archiveNr)
                    }
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    showMessage.value = error
                }
            })
    }

    // Stela V2 navigation (VSP-1810). At most one fetch is in flight (the isBusy guard
    // in loadFilesOf), so the generation guard MyFilesViewModel needs is unnecessary
    // here — a fetch either commits or falls back to V1.
    private fun loadFilesOfV2(record: Record, folderId: Int, archiveNr: String, folderLinkId: Int) {
        isBusy.value = true
        fileRepository.getChildRecordsOfV2(folderId, object : IFolderChildrenListener {

            override fun onSuccess(records: List<Record>) {
                isBusy.value = false
                if (BuildConfig.DEBUG) Log.d(TAG, "Children of folder $folderId served by V2")
                // V2 returns no parent name — the listed folder's own displayName is the
                // same value V1's getLeanItems envelope carried.
                onFolderNameChanged.value = record.displayName
                // The endpoint has no sort param (sort is a folder attribute) — apply
                // this screen's fixed sort locally, like iOS.
                val sortedRecords = records.sortedWith(SortType.NAME_ASCENDING.toComparator())
                sortedRecords.forEach { it.parentFolderArchiveNr = archiveNr }
                existsRecords.value = sortedRecords.isNotEmpty()
                onRecordsRetrieved.value = sortedRecords.toMutableList()
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                // V1 failsafe: nothing can supersede this fetch, so no ordering hazard.
                if (BuildConfig.DEBUG) Log.d(
                    TAG, "V2 children of folder $folderId failed ($error), falling back to V1"
                )
                loadFilesOfV1(archiveNr, folderLinkId)
            }
        })
    }

    private fun getRecords(
        recordVOs: List<RecordVO>, parentFolderArchiveNr: String
    ): MutableList<Record> {
        val records = ArrayList<Record>()
        for (recordVO in recordVOs) {
            val record = Record(recordVO)
            record.parentFolderArchiveNr = parentFolderArchiveNr
            records.add(record)
        }
        return records
    }

    /**
     * @return true if Up navigation completed successfully, false otherwise.
     */
    fun onNavigateUp(): Boolean {
        // Popping the record of the current folder
        folderPathStack.pop()
        if (folderPathStack.isEmpty()) {
            return false
        } else {
            val previousFolder = folderPathStack.peek()
            loadFilesOf(previousFolder)
        }
        return true
    }

    fun getCurrentFolder(): Record? = folderPathStack.peek()

    fun getOnFolderNameChanged(): MutableLiveData<String> = onFolderNameChanged

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getShowMessage(): LiveData<String> = showMessage

    fun getOnRecordsRetrieved(): LiveData<MutableList<Record>> = onRecordsRetrieved

    fun getOnFileViewRequest(): MutableLiveData<ArrayList<Record>> = onFileViewRequest
}
