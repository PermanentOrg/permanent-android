package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.myFiles.SortType
import java.util.*

class PublicFolderViewModel(application: Application) : ObservableAndroidViewModel(application) {

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
