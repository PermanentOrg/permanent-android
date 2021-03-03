package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.Constants
import org.permanent.permanent.models.Download
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

    private lateinit var lifecycleOwner: LifecycleOwner
    private lateinit var downloadQueue: DownloadQueue
    val isRoot = MutableLiveData(true)
    private val folderName = MutableLiveData(Constants.MY_FILES_FOLDER)
    private val isBusy = MutableLiveData(false)
    private val showMessage = MutableLiveData<String>()
    var existsShares = MutableLiveData(false)
    private var folderPathStack: Stack<DownloadableRecord> = Stack()
    private val onRecordsRetrieved = SingleLiveEvent<MutableList<DownloadableRecord>>()
    private val onRootSharesNeeded = SingleLiveEvent<Void>()
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

    fun download(record: DownloadableRecord) {
        val download = downloadQueue.enqueueNewDownloadFor(record)
        record.observe(lifecycleOwner, download)
    }

    fun onRecordClick(record: DownloadableRecord) {
        if (record.type == RecordType.FOLDER) {
            folderPathStack.push(record)
            loadFilesOf(record)
        }
    }

    private fun loadFilesOf(record: DownloadableRecord) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        val archiveNr = record.archiveNr
        val folderLinkId = record.folderLinkId
        if (archiveNr != null && folderLinkId != null) {
            isBusy.value = true
            fileRepository.getChildRecordsOf(archiveNr, folderLinkId,
                SortType.NAME_ASCENDING.toBackendString(),
                object : IFileRepository.IOnRecordsRetrievedListener {
                    override fun onSuccess(recordVOs: List<RecordVO>?) {
                        isBusy.value = false
                        isRoot.value = false
                        folderName.value = record.displayName
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

    override fun onFinished(download: Download) {
        showMessage.value = "Downloaded ${download.getDisplayName()}"
    }

    override fun onFinished(upload: Upload, succeeded: Boolean) { // Not needed
    }
}