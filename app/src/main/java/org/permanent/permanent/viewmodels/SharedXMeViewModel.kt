package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkInfo
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.models.Download
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.models.Upload
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.myFiles.OnFinishedListener
import org.permanent.permanent.ui.myFiles.SortType
import org.permanent.permanent.ui.myFiles.download.DownloadQueue
import org.permanent.permanent.ui.myFiles.download.DownloadableRecord
import java.util.*

class SharedXMeViewModel(application: Application
) : ObservableAndroidViewModel(application), OnFinishedListener {

    private val appContext: Context = application.applicationContext
    private lateinit var lifecycleOwner: LifecycleOwner
    private lateinit var downloadQueue: DownloadQueue
    val isRoot = MutableLiveData(true)
    private val folderName = MutableLiveData(Constants.MY_FILES_FOLDER)
    private val isBusy = MutableLiveData(false)
    private val showMessage = SingleLiveEvent<String>()
    var existsShares = MutableLiveData(false)
    private var folderPathStack: Stack<DownloadableRecord> = Stack()
    private val onRecordsRetrieved = SingleLiveEvent<MutableList<DownloadableRecord>>()
    private val onRootSharesNeeded = SingleLiveEvent<Void>()
    private val onFileViewRequest = SingleLiveEvent<Record>()
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)

    fun setLifecycleOwner(lifecycleOwner: LifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner
        downloadQueue = DownloadQueue(getApplication(), lifecycleOwner, this)
    }

    fun getIsRoot(): MutableLiveData<Boolean> {
        return isRoot
    }

    fun getFolderName(): MutableLiveData<String> {
        return folderName
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getShowMessage(): LiveData<String> {
        return showMessage
    }

    fun getOnRecordsRetrieved(): LiveData<MutableList<DownloadableRecord>> {
        return onRecordsRetrieved
    }

    fun getOnRootSharesNeeded(): LiveData<Void> {
        return onRootSharesNeeded
    }

    fun getOnFileViewRequest(): LiveData<Record> = onFileViewRequest

    fun download(downloadableRecord: DownloadableRecord) {
        val download = downloadQueue.enqueueNewDownloadFor(downloadableRecord)
        downloadableRecord.observe(download, lifecycleOwner)
    }

    fun onRecordClick(downloadableRecord: DownloadableRecord) {
        if (downloadableRecord.type == RecordType.FOLDER) {
            folderPathStack.push(downloadableRecord)
            loadFilesOf(downloadableRecord)
        } else {
            onFileViewRequest.value = downloadableRecord
        }
    }

    private fun loadFilesOf(downloadableRecord: DownloadableRecord) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        val archiveNr = downloadableRecord.archiveNr
        val folderLinkId = downloadableRecord.folderLinkId
        if (archiveNr != null && folderLinkId != null) {
            isBusy.value = true
            fileRepository.getChildRecordsOf(archiveNr, folderLinkId,
                SortType.NAME_ASCENDING.toBackendString(),
                object : IFileRepository.IOnRecordsRetrievedListener {
                    override fun onSuccess(recordVOs: List<RecordVO>?) {
                        isBusy.value = false
                        isRoot.value = false
                        folderName.value = downloadableRecord.displayName
                        existsShares.value = !recordVOs.isNullOrEmpty()
                        recordVOs?.let { onRecordsRetrieved.value = getDownloadableRecords(recordVOs) }
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        showMessage.value = error
                    }
                })
        }
    }

    private fun getDownloadableRecords(recordVOs: List<RecordVO>): MutableList<DownloadableRecord> {
        val downloadableRecords = ArrayList<DownloadableRecord>()
        for (recordVO in recordVOs) {
            downloadableRecords.add(DownloadableRecord(recordVO))
        }
        return downloadableRecords
    }

    fun onBackBtnClick() {
        // This is the record of the current folder but we need his parent
        folderPathStack.pop()
        if (folderPathStack.isEmpty()) {
            onRootSharesNeeded.call()
        } else {
            val previousFolder = folderPathStack.pop()
            folderPathStack.push(previousFolder)
            loadFilesOf(previousFolder)
        }
    }

    fun cancelDownloadOf(downloadableRecord: DownloadableRecord) {
        downloadableRecord.cancel()
        downloadQueue.removeDownload(downloadableRecord.download)
    }

    override fun onFinished(download: Download, state: WorkInfo.State) {
        if (state == WorkInfo.State.SUCCEEDED)
            showMessage.value = "Downloaded ${download.getDisplayName()}"
        else if (state == WorkInfo.State.FAILED)
            showMessage.value = appContext.getString(R.string.generic_error)
    }

    override fun onFailed(message: String) {
        showMessage.value = message
    }

    override fun onQuotaExceeded() {} // Not needed

    override fun onFinished(upload: Upload, succeeded: Boolean) {} // Not needed
}