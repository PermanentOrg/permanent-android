package org.permanent.permanent.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.permanent.permanent.Constants
import org.permanent.permanent.CurrentArchivePermissionsManager
import org.permanent.permanent.R
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Archive
import org.permanent.permanent.models.Download
import org.permanent.permanent.models.NavigationFolder
import org.permanent.permanent.models.NavigationFolderIdentifier
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.models.Upload
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.myFiles.CancelListener
import org.permanent.permanent.ui.myFiles.OnFinishedListener
import org.permanent.permanent.ui.myFiles.RecordListener
import org.permanent.permanent.ui.myFiles.SortType
import org.permanent.permanent.ui.myFiles.download.DownloadQueue
import org.permanent.permanent.ui.myFiles.upload.UploadsAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Stack

class SharedXMeViewModel(application: Application) : SelectionViewModel(application),
    CancelListener, OnFinishedListener, RecordListener {

    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private lateinit var lifecycleOwner: LifecycleOwner
    private var refreshJob: Job? = null

    val isRoot = MutableLiveData(true)
    private val isCreateAvailable = MutableLiveData(true)
    private val isListViewMode = MutableLiveData(prefsHelper.isListViewMode())
    private var existsDownloads = MutableLiveData(false)
    private val folderName = MutableLiveData(Constants.MY_FILES_FOLDER)
    private val currentSortType: MutableLiveData<SortType> =
        MutableLiveData(SortType.NAME_ASCENDING)
    private val sortName: MutableLiveData<String> =
        MutableLiveData(SortType.NAME_ASCENDING.toUIString())
    private var folderPathStack: Stack<Record> = Stack()

    private val showQuotaExceeded = SingleLiveEvent<Void?>()
    private val onShowAddOptionsFragment = SingleLiveEvent<NavigationFolderIdentifier>()
    private val onShowRecordOptionsFragment = SingleLiveEvent<Record>()
    private val onDownloadsRetrieved = SingleLiveEvent<MutableList<Download>>()
    private val onDownloadFinished = SingleLiveEvent<Download>()
    private val onRecordsRetrieved = SingleLiveEvent<MutableList<Record>>()
    private val onRootSharesNeeded = SingleLiveEvent<Void?>()
    private val onChangeViewMode = MutableLiveData<Boolean>()
    private val onCancelAllUploads = SingleLiveEvent<Void?>()
    private val onShowSortOptionsFragment = SingleLiveEvent<SortType>()
    private val onFileViewRequest = SingleLiveEvent<Record>()
    private val showRelocationCancellationDialog = SingleLiveEvent<Void?>()
    private val onRecordSelected = SingleLiveEvent<Record>()
    private val openChecklistBottomSheet = SingleLiveEvent<Void?>()
    private var showScreenSimplified = MutableLiveData(false)

    private lateinit var downloadQueue: DownloadQueue
    private lateinit var uploadsAdapter: UploadsAdapter
    private lateinit var uploadsRecyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    fun initSwipeRefreshLayout(refreshLayout: SwipeRefreshLayout) {
        this.swipeRefreshLayout = refreshLayout
        swipeRefreshLayout.setOnRefreshListener { refreshCurrentFolder() }
    }

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
        loadFilesOf(currentFolder.value, currentSortType.value)
    }

    fun setShowScreenSimplified() {
        showScreenSimplified.value = true
    }

    fun initUploadsRecyclerView(rvUploads: RecyclerView, lifecycleOwner: LifecycleOwner) {
        uploadsRecyclerView = rvUploads
        this.lifecycleOwner = lifecycleOwner
        uploadsAdapter = UploadsAdapter(lifecycleOwner, this)
        uploadsRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = uploadsAdapter
        }
    }

    override fun onRecordClick(record: Record) {
        if (record.isProcessing) {
            return
        }
        if (record.type == RecordType.FOLDER) {
            if (showScreenSimplified.value == true) onRecordSelected.value = record
            currentFolder.value?.getUploadQueue()?.clearEnqueuedUploadsAndRemoveTheirObservers()
            folderPathStack.push(record)
            currentFolder.value = NavigationFolder(appContext, record)
            isCreateAvailable.value =
                record.accessRole != AccessRole.VIEWER && CurrentArchivePermissionsManager.instance.isCreateAvailable()
            loadEnqueuedUploads(currentFolder.value, lifecycleOwner)
            loadFilesOf(currentFolder.value, currentSortType.value)
        } else {
            if (showScreenSimplified.value == false) {
                onFileViewRequest.value = record
            }
        }
    }

    override fun onRecordOptionsClick(record: Record) {
        onShowRecordOptionsFragment.value = record
    }

    fun onBackBtnClick() {
        if (isRelocationMode.value == true && folderPathStack.size == 1) { // There is only the root
            showRelocationCancellationDialog.call()
        } else {
            navigateBack()
        }
    }

    internal fun navigateBack() {
        currentFolder.value?.getUploadQueue()?.clearEnqueuedUploadsAndRemoveTheirObservers()
        // Popping the record of the current folder
        folderPathStack.pop()
        if (folderPathStack.isEmpty()) {
            onRootSharesNeeded.call()
        } else {
            val previousFolder = folderPathStack.peek()
            currentFolder.value = NavigationFolder(appContext, previousFolder)
            isCreateAvailable.value =
                previousFolder.accessRole != AccessRole.VIEWER && CurrentArchivePermissionsManager.instance.isCreateAvailable()
            loadEnqueuedUploads(currentFolder.value, lifecycleOwner)
            loadFilesOf(currentFolder.value, currentSortType.value)
        }
    }

    private fun loadFilesOf(folder: NavigationFolder?, sortType: SortType?) {
        val archiveNr = folder?.getArchiveNr()
        val folderLinkId = folder?.getFolderIdentifier()?.folderLinkId
        if (archiveNr != null && folderLinkId != null) {
            swipeRefreshLayout.isRefreshing = true
            fileRepository.getChildRecordsOf(archiveNr,
                folderLinkId,
                sortType?.toBackendString(),
                object : IFileRepository.IOnRecordsRetrievedListener {

                    override fun onSuccess(parentFolderName: String?, recordVOs: List<RecordVO>?) {
                        swipeRefreshLayout.isRefreshing = false
                        isRoot.value = false
                        folderName.value = folder.getDisplayName()
                        existsFiles.value = !recordVOs.isNullOrEmpty()
                        recordVOs?.let { onRecordsRetrieved.value = getRecords(recordVOs) }
                    }

                    override fun onFailed(error: String?) {
                        swipeRefreshLayout.isRefreshing = false
                        error?.let { showMessage.value = it }
                    }
                })
        }
    }

    private fun loadEnqueuedUploads(folder: NavigationFolder?, lifecycleOwner: LifecycleOwner) {
        folder?.newUploadQueue(lifecycleOwner, this)?.getEnqueuedUploadsLiveData()
            ?.let { enqueuedUploadsLiveData ->
                enqueuedUploadsLiveData.observe(lifecycleOwner) { enqueuedUploads ->
                    uploadsAdapter.set(enqueuedUploads)
                }
            }
    }

    private fun loadEnqueuedDownloads(lifecycleOwner: LifecycleOwner) {
        downloadQueue = DownloadQueue(appContext, lifecycleOwner, this)
        downloadQueue.getEnqueuedDownloadsLiveData().let { enqueuedDownloadsLiveData ->
            enqueuedDownloadsLiveData.observe(lifecycleOwner) { enqueuedDownloads ->
                onDownloadsRetrieved.value = enqueuedDownloads
            }
        }
    }

    fun onAddFabClick() {
        onShowAddOptionsFragment.value = currentFolder.value?.getFolderIdentifier()
    }

    fun onChecklistFabClick() {
        openChecklistBottomSheet.call()
    }

    fun upload(uris: List<Uri>) {
        currentFolder.value?.getUploadQueue()?.upload(uris)
    }

    private fun getRecords(recordVOs: List<RecordVO>): MutableList<Record> {
        val records = ArrayList<Record>()
        for (recordVO in recordVOs) {
            val record = Record(recordVO)
            record.displayInShares = true
            records.add(record)
        }
        return records
    }

    fun onViewModeBtnClick() {
        isListViewMode.value = !isListViewMode.value!!
        prefsHelper.saveIsListViewMode(isListViewMode.value!!)
        onChangeViewMode.value = isListViewMode.value
    }

    fun onSortOptionsClick() {
        onShowSortOptionsFragment.value = currentSortType.value
    }

    fun download(record: Record) {
        downloadQueue.enqueueNewDownloadFor(record)
    }

    fun cancelAllUploads() {
        currentFolder.value?.getUploadQueue()?.clear()
    }

    fun onCancelAllBtnClick() {
        onCancelAllUploads.call()
    }

    override fun onCancelClick(upload: Upload) {
        val uploadQueue = currentFolder.value?.getUploadQueue()
        uploadQueue?.prepareToRequeueUploadsExcept(upload)
        upload.cancel()
        uploadQueue?.enqueuePendingUploads(ExistingWorkPolicy.REPLACE)
    }

    override fun onFinished(upload: Upload, succeeded: Boolean) {
        currentFolder.value?.getUploadQueue()?.removeFinishedUpload(upload)
        uploadsAdapter.remove(upload)

        if (succeeded) addFakeItemToFilesList(upload)
        if (uploadsAdapter.itemCount == 0) {
            refreshJob?.cancel()
            refreshJob = viewModelScope.launch {
                delay(MyFilesViewModel.MILLIS_UNTIL_REFRESH_AFTER_UPLOAD)
                refreshCurrentFolder()
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun addFakeItemToFilesList(upload: Upload?) {
        val sdf = SimpleDateFormat("yyyy-M-dd")
        val currentDate = sdf.format(Date())
        val fakeRecordInfo = RecordVO()
        fakeRecordInfo.displayDT = currentDate
        fakeRecordInfo.displayName = upload?.getDisplayName()
        val fakeRecord = Record(fakeRecordInfo)
        fakeRecord.type = RecordType.FILE
        onNewTemporaryFiles.value = mutableListOf(fakeRecord)
        existsFiles.value = true
    }

    fun refreshCurrentFolder() {
        refreshJob?.cancel()
        if (folderPathStack.isEmpty()) {
            onRootSharesNeeded.call()
            swipeRefreshLayout.isRefreshing = false
        } else {
            loadFilesOf(currentFolder.value, currentSortType.value)
        }
    }

    override fun onCancelClick(download: Download) {
        download.cancel()
        downloadQueue.removeDownload(download)
    }

    override fun onFinished(download: Download, state: WorkInfo.State) {
        onDownloadFinished.value = download
        if (state == WorkInfo.State.SUCCEEDED) showMessage.value =
            "Downloaded ${download.getDisplayName()}"
        else if (state == WorkInfo.State.FAILED) showMessage.value =
            appContext.getString(R.string.generic_error)
    }

    override fun onFailedUpload(message: String) {
        showMessage.value = message
    }

    override fun onQuotaExceeded() {
        showQuotaExceeded.call()
    }

    fun delete(record: Record) {
        swipeRefreshLayout.isRefreshing = true
        fileRepository.deleteRecords(mutableListOf(record), object : IResponseListener {
            override fun onSuccess(message: String?) {
                swipeRefreshLayout.isRefreshing = false
                if (record.type == RecordType.FOLDER) showMessage.value =
                    appContext.getString(R.string.my_files_folder_deleted)
                else showMessage.value = appContext.getString(R.string.my_files_file_deleted)
                refreshJob = viewModelScope.launch {
                    delay(MyFilesViewModel.MILLIS_UNTIL_REFRESH_AFTER_DELETE)
                    refreshCurrentFolder()
                }
            }

            override fun onFailed(error: String?) {
                swipeRefreshLayout.isRefreshing = false
                showMessage.value = error
            }
        })
    }

    fun unshare(record: Record) {
        val currentArchiveId = PreferencesHelper(
            appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        ).getCurrentArchiveId()

        swipeRefreshLayout.isRefreshing = true
        fileRepository.unshareRecord(record, currentArchiveId, object : IResponseListener {
            override fun onSuccess(message: String?) {
                swipeRefreshLayout.isRefreshing = false
                refreshCurrentFolder()
                if (record.type == RecordType.FOLDER) showMessage.value =
                    appContext.getString(R.string.my_files_folder_unshared)
                else showMessage.value = appContext.getString(R.string.my_files_file_unshared)
            }

            override fun onFailed(error: String?) {
                swipeRefreshLayout.isRefreshing = false
                showMessage.value = error
            }
        })
    }

    fun cancelRelocationMode() {
        onCancelRelocationBtnClick()
    }

    fun uploadFilesToFolder(folder: Record?, uris: ArrayList<Uri>) {
        folder?.let {
            onRecordClick(it)
            upload(uris)
        }
    }


    override fun onRecordCheckBoxClick(record: Record) {
        super.onRecordChecked(record)
    }

    fun onSelectAllBtnClick() {
        super.onSelectAllRecords(onRecordsRetrieved.value!!)
    }

    override fun onRecordDeleteClick(record: Record) {}

    fun getIsListViewMode(): MutableLiveData<Boolean> = isListViewMode

    fun getExistsUploads(): MutableLiveData<Boolean> = uploadsAdapter.getExistsUploads()

    fun getExistsDownloads(): MutableLiveData<Boolean> = existsDownloads

    fun getFolderName(): MutableLiveData<String> = folderName

    fun getSortName(): MutableLiveData<String> = sortName

    fun getShowMessage(): LiveData<String> = showMessage

    fun getIsCreateAvailable(): LiveData<Boolean> = isCreateAvailable

    fun getOnShowQuotaExceeded(): SingleLiveEvent<Void?> = showQuotaExceeded

    fun getOnNewTemporaryFiles(): MutableLiveData<MutableList<Record>> = onNewTemporaryFiles

    fun getOnShowAddOptionsFragment(): MutableLiveData<NavigationFolderIdentifier> =
        onShowAddOptionsFragment

    fun getOnCancelAllUploads(): SingleLiveEvent<Void?> = onCancelAllUploads

    fun getOnDownloadsRetrieved(): MutableLiveData<MutableList<Download>> = onDownloadsRetrieved

    fun getOnDownloadFinished(): MutableLiveData<Download> = onDownloadFinished

    fun getOnRecordsRetrieved(): LiveData<MutableList<Record>> = onRecordsRetrieved

    fun getOnRootSharesNeeded(): LiveData<Void?> = onRootSharesNeeded

    fun getOnChangeViewMode(): MutableLiveData<Boolean> = onChangeViewMode

    fun getOnRecordSelected(): MutableLiveData<Record> = onRecordSelected

    fun getShowScreenSimplified(): MutableLiveData<Boolean> = showScreenSimplified

    fun getOnFileViewRequest(): LiveData<Record> = onFileViewRequest

    fun getShowRelocationCancellationDialog(): LiveData<Void?> = showRelocationCancellationDialog

    fun getOnShowSortOptionsFragment(): MutableLiveData<SortType> = onShowSortOptionsFragment

    fun getIsRelocationMode(): MutableLiveData<Boolean> = isRelocationMode

    fun getIsSelectionMode(): MutableLiveData<Boolean> = isSelectionMode

    fun getOnShowRecordOptionsFragment(): MutableLiveData<Record> = onShowRecordOptionsFragment

    fun getOpenChecklistBottomSheet(): MutableLiveData<Void?> = openChecklistBottomSheet

    fun getCurrentArchive() : Archive = prefsHelper.getCurrentArchive()
}
