package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.R
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.network.IRecordListener
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.myFiles.SortType

class PublicArchiveViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private var existsRecords = MutableLiveData(false)
    private val isBusy = MutableLiveData(false)
    private val showMessage = SingleLiveEvent<String>()
    private var currentFolder: Record? = null
    private val onRecordsRetrieved = SingleLiveEvent<MutableList<Record>>()
    private val onFileViewRequest = SingleLiveEvent<ArrayList<Record>>()
    private val onFolderViewRequest = SingleLiveEvent<Record>()
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)
    private var archiveNr: String? = null

    fun setArchiveNr(archiveNr: String?) {
        this.archiveNr = archiveNr
    }

    fun getRootRecords() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        isBusy.value = true
        fileRepository.getPublicRoot(archiveNr, object : IRecordListener {
            override fun onSuccess(record: Record) {
                isBusy.value = false
                currentFolder = record
                loadFilesOf(record)
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showMessage.value = error
            }
        })
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
                    override fun onSuccess(recordVOs: List<RecordVO>?) {
                        isBusy.value = false
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
        recordVOs: List<RecordVO>,
        parentFolderArchiveNr: String
    ): MutableList<Record> {
        val records = ArrayList<Record>()
        for (recordVO in recordVOs) {
            val record = Record(recordVO)
            record.parentFolderArchiveNr = parentFolderArchiveNr
            records.add(record)
        }
        return records
    }

    fun onCopyLinkBtnClick() {
        val sharableLink = BuildConfig.BASE_URL + "p/archive/" + prefsHelper.getCurrentArchiveNr() +
                "/" + currentFolder?.archiveNr + "/" + currentFolder?.folderLinkId
        val clipboard = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText(
            appContext.getString(R.string.share_management_share_link), sharableLink
        )
        clipboard.setPrimaryClip(clip)
        showMessage.value = appContext.getString(R.string.share_management_link_copied)
    }

    fun onRecordClick(record: Record) {
        if (record.type == RecordType.FOLDER) {
            onFolderViewRequest.value = record
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

    fun getExistsRecords(): MutableLiveData<Boolean> = existsRecords

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getShowMessage(): LiveData<String> = showMessage

    fun getOnRecordsRetrieved(): LiveData<MutableList<Record>> = onRecordsRetrieved

    fun getOnFileViewRequest(): MutableLiveData<ArrayList<Record>> = onFileViewRequest

    fun getOnFolderViewRequest(): LiveData<Record> = onFolderViewRequest
}
