package org.permanent.permanent.viewmodels

import android.app.Application
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
import org.permanent.permanent.ui.myFiles.CancelListener
import org.permanent.permanent.ui.myFiles.OnFinishedListener
import org.permanent.permanent.ui.myFiles.SortType
import org.permanent.permanent.ui.myFiles.download.DownloadQueue
import java.util.*

class SharedXMeViewModel(application: Application) : ObservableAndroidViewModel(application),
    CancelListener, OnFinishedListener {

    private val appContext = application.applicationContext
    private lateinit var lifecycleOwner: LifecycleOwner
    private val isRoot = MutableLiveData(true)
    private val isListViewMode = MutableLiveData(true)
    private var existsShares = MutableLiveData(false)
    private var existsDownloads = MutableLiveData(false)
    private val folderName = MutableLiveData(Constants.MY_FILES_FOLDER)
    private val currentSortType: MutableLiveData<SortType> =
        MutableLiveData(SortType.NAME_ASCENDING)
    private val sortName: MutableLiveData<String> =
        MutableLiveData(SortType.NAME_ASCENDING.toUIString())
    private val isSortedAsc = MutableLiveData(true)
    private val isBusy = MutableLiveData(false)
    private val showMessage = SingleLiveEvent<String>()
    private var folderPathStack: Stack<Record> = Stack()
    private lateinit var downloadQueue: DownloadQueue
    private val onDownloadsRetrieved = SingleLiveEvent<MutableList<Download>>()
    private val onDownloadFinished = SingleLiveEvent<Download>()
    private val onRecordsRetrieved = SingleLiveEvent<MutableList<Record>>()
    private val onRootSharesNeeded = SingleLiveEvent<Void>()
    private val onChangeViewMode = SingleLiveEvent<Boolean>()
    private val onShowSortOptionsFragment = SingleLiveEvent<SortType>()
    private val onFileViewRequest = SingleLiveEvent<Record>()
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)

    fun setLifecycleOwner(lifecycleOwner: LifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner
        loadEnqueuedDownloads(lifecycleOwner)
    }

    fun setIsListViewMode(isListViewMode: Boolean) {
        this.isListViewMode.value = isListViewMode
    }

    fun setExistsDownloads(existsDownloads: MutableLiveData<Boolean>) {
        this.existsDownloads = existsDownloads
    }

    fun setSortType(sortType: SortType) {
        currentSortType.value = sortType
        sortName.value = sortType.toUIString()
        isSortedAsc.value = sortType == SortType.FILE_TYPE_ASCENDING
                || sortType == SortType.DATE_ASCENDING
                || sortType == SortType.NAME_ASCENDING
        loadFilesOf(folderPathStack.peek(), currentSortType.value)
    }

    fun onRecordClick(record: Record) {
        if (record.type == RecordType.FOLDER) {
            folderPathStack.push(record)
            loadFilesOf(record, currentSortType.value)
        } else {
            onFileViewRequest.value = record
        }
    }

    private fun loadFilesOf(record: Record, sortType: SortType?) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        val archiveNr = record.archiveNr
        val folderLinkId = record.folderLinkId
        if (archiveNr != null && folderLinkId != null) {
            isBusy.value = true
            fileRepository.getChildRecordsOf(archiveNr, folderLinkId, sortType?.toBackendString(),
                object : IFileRepository.IOnRecordsRetrievedListener {
                    override fun onSuccess(recordVOs: List<RecordVO>?) {
                        isBusy.value = false
                        isRoot.value = false
                        folderName.value = record.displayName
                        existsShares.value = !recordVOs.isNullOrEmpty()
                        recordVOs?.let { onRecordsRetrieved.value = getRecords(recordVOs) }
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        showMessage.value = error
                    }
                })
        }
    }

    private fun loadEnqueuedDownloads(lifecycleOwner: LifecycleOwner) {
        downloadQueue = DownloadQueue(appContext, lifecycleOwner, this)
        downloadQueue.getEnqueuedDownloadsLiveData().let { enqueuedDownloadsLiveData ->
            enqueuedDownloadsLiveData.observe(lifecycleOwner, { enqueuedDownloads ->
                onDownloadsRetrieved.value = enqueuedDownloads
            })
        }
    }

    private fun getRecords(recordVOs: List<RecordVO>): MutableList<Record> {
        val records = ArrayList<Record>()
        for (recordVO in recordVOs) {
            records.add(Record(recordVO))
        }
        return records
    }

    fun onBackBtnClick() {
        // Popping the record of the current folder
        folderPathStack.pop()
        if (folderPathStack.isEmpty()) {
            onRootSharesNeeded.call()
        } else {
            val previousFolder = folderPathStack.peek()
            loadFilesOf(previousFolder, currentSortType.value)
        }
    }

    fun onViewModeBtnClick() {
        isListViewMode.value = !isListViewMode.value!!
        onChangeViewMode.value = isListViewMode.value
    }

    fun onSortOptionsClick() {
        onShowSortOptionsFragment.value = currentSortType.value
    }

    fun download(record: Record) {
        downloadQueue.enqueueNewDownloadFor(record)
    }

    override fun onCancelClick(download: Download) {
        download.cancel()
        downloadQueue.removeDownload(download)
    }

    override fun onFinished(download: Download, state: WorkInfo.State) {
        onDownloadFinished.value = download
        if (state == WorkInfo.State.SUCCEEDED)
            showMessage.value = "Downloaded ${download.getDisplayName()}"
        else if (state == WorkInfo.State.FAILED)
            showMessage.value = appContext.getString(R.string.generic_error)
    }

    override fun onCancelClick(upload: Upload) {}

    override fun onFinished(upload: Upload, succeeded: Boolean) {}

    override fun onFailedUpload(message: String) {}

    override fun onQuotaExceeded() {}

    fun getIsRoot(): MutableLiveData<Boolean> = isRoot

    fun getIsListViewMode(): MutableLiveData<Boolean> = isListViewMode

    fun getExistsShares(): MutableLiveData<Boolean> = existsShares

    fun getExistsDownloads(): MutableLiveData<Boolean> = existsDownloads

    fun getFolderName(): MutableLiveData<String> = folderName

    fun getSortName(): MutableLiveData<String> = sortName

    fun getIsSortedAsc(): MutableLiveData<Boolean> = isSortedAsc

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getShowMessage(): LiveData<String> = showMessage

    fun getOnDownloadsRetrieved(): MutableLiveData<MutableList<Download>> = onDownloadsRetrieved

    fun getOnDownloadFinished(): MutableLiveData<Download> = onDownloadFinished

    fun getOnRecordsRetrieved(): LiveData<MutableList<Record>> = onRecordsRetrieved

    fun getOnRootSharesNeeded(): LiveData<Void> = onRootSharesNeeded

    fun getOnChangeViewMode(): SingleLiveEvent<Boolean> = onChangeViewMode

    fun getOnFileViewRequest(): LiveData<Record> = onFileViewRequest

    fun getOnShowSortOptionsFragment(): MutableLiveData<SortType> = onShowSortOptionsFragment
}
