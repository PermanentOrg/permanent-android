package org.permanent.permanent.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.permanent.permanent.Constants
import org.permanent.permanent.CurrentArchivePermissionsManager
import org.permanent.permanent.R
import org.permanent.permanent.models.*
import org.permanent.permanent.network.IRecordListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.repositories.*
import org.permanent.permanent.ui.myFiles.*
import org.permanent.permanent.ui.myFiles.download.DownloadQueue
import org.permanent.permanent.ui.myFiles.upload.UploadsAdapter
import java.text.SimpleDateFormat
import java.util.*

open class MyFilesViewModel(application: Application) : ObservableAndroidViewModel(application),
    RecordListener, CancelListener, OnFinishedListener {

    private val TAG = MyFilesViewModel::class.java.simpleName
    private val appContext = application.applicationContext
    private val folderName = MutableLiveData(Constants.PRIVATE_FILES)
    private var refreshJob: Job? = null
    private val isRoot = MutableLiveData(true)
    private val sortName: MutableLiveData<String> =
        MutableLiveData(SortType.NAME_ASCENDING.toUIString())
    private val isListViewMode = MutableLiveData(true)
    private val isCreateAvailable = CurrentArchivePermissionsManager.instance.isCreateAvailable()
    private val isRelocationMode = MutableLiveData(false)
    private val relocationType = MutableLiveData<RelocationType>()
    private val currentSortType: MutableLiveData<SortType> =
        MutableLiveData(SortType.NAME_ASCENDING)
    private var currentFolder = MutableLiveData<NavigationFolder>()
    private var mobileUploadsFolder = MutableLiveData<NavigationFolder>()
    private val existsFiles = MutableLiveData(false)
    private var existsDownloads = MutableLiveData(false)
    private var showEmptyFolder = MutableLiveData(false)
    private val recordToRelocate = MutableLiveData<Record>()
    protected val showMessage = SingleLiveEvent<String>()
    private val showQuotaExceeded = SingleLiveEvent<Void>()
    private val onChangeViewMode = SingleLiveEvent<Boolean>()
    private val onCancelAllUploads = SingleLiveEvent<Void>()
    private val onDownloadsRetrieved = SingleLiveEvent<MutableList<Download>>()
    private val onDownloadFinished = SingleLiveEvent<Download>()
    private val onRecordsRetrieved = SingleLiveEvent<List<Record>>()
    private val onNewTemporaryFile = SingleLiveEvent<Record>()
    private val onShowRecordSearchFragment = SingleLiveEvent<Void>()
    private val onShowAddOptionsFragment = SingleLiveEvent<NavigationFolderIdentifier>()
    private val onShowRecordOptionsFragment = SingleLiveEvent<Record>()
    private val onShowSortOptionsFragment = SingleLiveEvent<SortType>()
    private val onRecordDeleteRequest = SingleLiveEvent<Record>()
    private val onFileViewRequest = SingleLiveEvent<ArrayList<Record>>()
    private val onPhotoSelected = SingleLiveEvent<Record>()
    private var showScreenSimplified = MutableLiveData(false)

    protected var fileRepository: IFileRepository = FileRepositoryImpl(application)
    protected var accountRepository: IAccountRepository = AccountRepositoryImpl(application)
    protected var folderPathStack: Stack<Record> = Stack()
    private lateinit var uploadsAdapter: UploadsAdapter
    private lateinit var downloadQueue: DownloadQueue
    private lateinit var uploadsRecyclerView: RecyclerView
    protected lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var fragmentManager: FragmentManager
    protected lateinit var lifecycleOwner: LifecycleOwner

    fun set(fragmentManager: FragmentManager) {
        this.fragmentManager = fragmentManager
    }

    fun setIsListViewMode(isListViewMode: Boolean) {
        this.isListViewMode.value = isListViewMode
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

    fun initSwipeRefreshLayout(refreshLayout: SwipeRefreshLayout) {
        this.swipeRefreshLayout = refreshLayout
        swipeRefreshLayout.setOnRefreshListener { refreshCurrentFolder() }
    }

    open fun loadRootFiles() {
        swipeRefreshLayout.isRefreshing = true
        fileRepository.getMyFilesRecord(object : IRecordListener {
            override fun onSuccess(record: Record) {
                swipeRefreshLayout.isRefreshing = false
                folderPathStack.push(record)
                loadFilesAndUploadsOf(record)
                loadEnqueuedDownloads(lifecycleOwner)
            }

            override fun onFailed(error: String?) {
                swipeRefreshLayout.isRefreshing = false
                error?.let { showMessage.value = it }
            }
        })
    }

    fun setExistsDownloads(existsDownloads: MutableLiveData<Boolean>) {
        this.existsDownloads = existsDownloads
    }

    fun setShowScreenSimplified() {
        showScreenSimplified.value = true
        swipeRefreshLayout.isRefreshing = false
        swipeRefreshLayout.isEnabled = false
    }

    fun refreshCurrentFolder() {
        refreshJob?.cancel()
        loadFilesOf(currentFolder.value, currentSortType.value)
    }

    private fun loadFilesOf(folder: NavigationFolder?, sortType: SortType?) {
        val archiveNr = folder?.getArchiveNr()
        val folderLinkId = folder?.getFolderIdentifier()?.folderLinkId
        if (archiveNr != null && folderLinkId != null) {
            swipeRefreshLayout.isRefreshing = true
            fileRepository.getChildRecordsOf(archiveNr, folderLinkId, sortType?.toBackendString(),
                object : IFileRepository.IOnRecordsRetrievedListener {
                    override fun onSuccess(recordVOs: List<RecordVO>?) {
                        swipeRefreshLayout.isRefreshing = false
                        val parentName = folder.getDisplayName()
                        isRoot.value =
                            parentName.equals(Constants.MY_FILES_FOLDER) || parentName.equals(
                                Constants.PUBLIC_FILES_FOLDER
                            )
                        folderName.value = when {
                            parentName.equals(Constants.MY_FILES_FOLDER) -> Constants.PRIVATE_FILES
                            parentName.equals(
                                Constants.PUBLIC_FILES_FOLDER
                            ) -> Constants.PUBLIC_FILES
                            else -> parentName
                        }

                        existsFiles.value = !recordVOs.isNullOrEmpty()
                        showEmptyFolder.value =
                            existsFiles.value == false && getExistsUploads().value == false
                        recordVOs?.let { onRecordsRetrieved.value = getRecords(recordVOs) }
                    }

                    override fun onFailed(error: String?) {
                        swipeRefreshLayout.isRefreshing = false
                        error?.let { showMessage.value = it }
                    }
                })
        }
    }

    private fun getRecords(recordVOs: List<RecordVO>): List<Record> {
        val records = ArrayList<Record>()
        for (recordVO in recordVOs) {
            records.add(Record(recordVO))
        }
        return records
    }

    fun onSearchClick() {
        onShowRecordSearchFragment.call()
    }

    fun onAddFabClick() {
        onShowAddOptionsFragment.value = currentFolder.value?.getFolderIdentifier()
    }

    override fun onRecordOptionsClick(record: Record) {
        onShowRecordOptionsFragment.value = record
    }

    override fun onRecordDeleteClick(record: Record) {
        onRecordDeleteRequest.value = record
    }

    fun onSortOptionsClick() {
        onShowSortOptionsFragment.value = currentSortType.value
    }

    fun onViewModeBtnClick() {
        isListViewMode.value = !isListViewMode.value!!
        onChangeViewMode.value = isListViewMode.value
    }

    override fun onRecordClick(record: Record) {
        if (record.isProcessing) {
            return
        }

        when {
            record.type == RecordType.FOLDER -> {
                currentFolder.value?.getUploadQueue()?.clearEnqueuedUploadsAndRemoveTheirObservers()
                mobileUploadsFolder.value?.getUploadQueue()?.clearEnqueuedUploadsAndRemoveTheirObservers()
                folderPathStack.push(record)
                loadFilesAndUploadsOf(record)
            }
            showScreenSimplified.value == true -> {
                onPhotoSelected.value = record
            }
            else -> {
                record.displayFirstInCarousel = true
                onFileViewRequest.value = getFilesForViewing(onRecordsRetrieved.value)
            }
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

    fun onBackBtnClick() {
        currentFolder.value?.getUploadQueue()?.clearEnqueuedUploadsAndRemoveTheirObservers()
        // Popping the record of the current folder
        folderPathStack.pop()
        val previousFolder = folderPathStack.peek()
        loadFilesAndUploadsOf(previousFolder)
    }

    protected fun loadFilesAndUploadsOf(record: Record) {
        currentFolder.value = NavigationFolder(appContext, record)
        loadEnqueuedUploads(currentFolder.value, lifecycleOwner)
        loadFilesOf(currentFolder.value, currentSortType.value)
    }

    private fun loadEnqueuedUploads(folder: NavigationFolder?, lifecycleOwner: LifecycleOwner) {
        folder?.newUploadQueue(lifecycleOwner, this)
            ?.getEnqueuedUploadsLiveData()?.let { enqueuedUploadsLiveData ->
                enqueuedUploadsLiveData.observe(lifecycleOwner) { enqueuedUploads ->
                    uploadsAdapter.set(enqueuedUploads)
                }
            }
    }

    protected fun loadEnqueuedDownloads(lifecycleOwner: LifecycleOwner) {
        downloadQueue = DownloadQueue(appContext, lifecycleOwner, this)
        downloadQueue.getEnqueuedDownloadsLiveData().let { enqueuedDownloadsLiveData ->
            enqueuedDownloadsLiveData.observe(lifecycleOwner) { enqueuedDownloads ->
                onDownloadsRetrieved.value = enqueuedDownloads
            }
        }
    }

    fun uploadToCurrentFolder(uris: List<Uri>) {
        currentFolder.value?.let { uploadTo(it, uris) }
    }

    private fun uploadTo(folder: NavigationFolder, uris: List<Uri>) {
        folder.getUploadQueue()?.upload(uris)
    }

    fun uploadToMobileUploads(uris: ArrayList<Uri>) {
        val mobileUploadsRecord: Record? = getMobileUploadsRecord()

        if (mobileUploadsRecord == null) {
            createMobileUploadsFolderAndUpload(uris)
        } else {
            onFolderCreated(mobileUploadsRecord, uris)
        }
    }

    private fun getMobileUploadsRecord(): Record? {
        val currentRecords = onRecordsRetrieved.value

        if (currentRecords != null && currentRecords.isNotEmpty()) {
            for (record in currentRecords) {
                if (record.displayName?.lowercase(Locale.getDefault()).equals(
                        MOBILE_UPLOADS_FOLDER_NAME.lowercase(Locale.getDefault())
                    )
                ) {
                    return record
                }
            }
        }
        return null
    }

    private fun createMobileUploadsFolderAndUpload(urisToUpload: List<Uri>) {
        val parentFolderIdentifier = currentFolder.value?.getFolderIdentifier()

        if (swipeRefreshLayout.isRefreshing) return

        if (parentFolderIdentifier != null) {
            swipeRefreshLayout.isRefreshing = true
            fileRepository.createFolder(
                parentFolderIdentifier,
                MOBILE_UPLOADS_FOLDER_NAME,
                object : IRecordListener {
                    override fun onSuccess(record: Record) {
                        swipeRefreshLayout.isRefreshing = false
                        onFolderCreated(record, urisToUpload)
                        refreshCurrentFolder()
                    }

                    override fun onFailed(error: String?) {
                        swipeRefreshLayout.isRefreshing = false
                        error?.let { showMessage.value = it }
                    }
                })
        }
    }

    private fun onFolderCreated(mobileUploadsFolderRecord: Record, urisToUpload: List<Uri>) {
        mobileUploadsFolder.value = NavigationFolder(appContext, mobileUploadsFolderRecord)
        mobileUploadsFolder.value?.newUploadQueue(lifecycleOwner, this)
        mobileUploadsFolder.value?.let { uploadTo(it, urisToUpload) }
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
            existsFiles.value = true
            refreshJob?.cancel()
            refreshJob = viewModelScope.launch {
                delay(MILLIS_UNTIL_REFRESH)
                refreshCurrentFolder()
            }
        }
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

    override fun onFailedUpload(message: String) {
        showMessage.value = message
    }

    override fun onQuotaExceeded() {
        showQuotaExceeded.call()
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
        onNewTemporaryFile.value = fakeRecord
    }

    fun setSortType(sortType: SortType) {
        currentSortType.value = sortType
        sortName.value = sortType.toUIString()
        loadFilesOf(currentFolder.value, currentSortType.value)
    }

    fun setRelocationMode(relocationPair: Pair<Record, RelocationType>) {
        recordToRelocate.value = relocationPair.first
        relocationType.value = relocationPair.second
        isRelocationMode.value = true
    }

    fun onRelocateBtnClick() {
        isRelocationMode.value = false
        val recordValue = recordToRelocate.value
        val folderLinkId = currentFolder.value?.getFolderIdentifier()?.folderLinkId
        val relocationTypeValue = relocationType.value
        if (recordValue != null && folderLinkId != null && relocationTypeValue != null) {
            swipeRefreshLayout.isRefreshing = true
            fileRepository.relocateRecord(recordValue, folderLinkId, relocationTypeValue,
                object : IResponseListener {
                    override fun onSuccess(message: String?) {
                        swipeRefreshLayout.isRefreshing = false
                        message?.let { showMessage.value = it }
                        onNewTemporaryFile.value = recordToRelocate.value
                        existsFiles.value = true
                    }

                    override fun onFailed(error: String?) {
                        swipeRefreshLayout.isRefreshing = false
                        error?.let { showMessage.value = it }
                    }
                })
        }
    }

    fun onCancelRelocationBtnClick() {
        isRelocationMode.value = false
    }

    fun delete(record: Record) {
        swipeRefreshLayout.isRefreshing = true
        fileRepository.deleteRecord(record, object : IResponseListener {
            override fun onSuccess(message: String?) {
                swipeRefreshLayout.isRefreshing = false
                refreshCurrentFolder()
                if (record.type == RecordType.FOLDER)
                    showMessage.value = appContext.getString(R.string.my_files_folder_deleted)
                else showMessage.value = appContext.getString(R.string.my_files_file_deleted)
            }

            override fun onFailed(error: String?) {
                swipeRefreshLayout.isRefreshing = false
                error?.let { showMessage.value = it }
            }
        })
    }

    fun registerDeviceForFCM() {
        val notificationsRepository: INotificationRepository =
            NotificationRepositoryImpl(appContext)

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e(TAG, "Fetching FCM token failed: ${task.exception}")
                    return@OnCompleteListener
                }
                notificationsRepository.registerDevice(task.result,
                    object : IResponseListener {

                        override fun onSuccess(message: String?) {
                        }

                        override fun onFailed(error: String?) {
                            Log.e(TAG, "Registering Device FCM token failed: $error")
                        }
                    })
            })
    }

    fun getFolderName(): MutableLiveData<String> = folderName

    fun getExistsDownloads(): MutableLiveData<Boolean> = existsDownloads

    fun getExistsUploads(): MutableLiveData<Boolean> = uploadsAdapter.getExistsUploads()

    fun getExistsFiles(): MutableLiveData<Boolean> = existsFiles

    fun getShowEmptyFolder(): MutableLiveData<Boolean> = showEmptyFolder

    fun getIsRoot(): MutableLiveData<Boolean> = isRoot

    fun getIsListViewMode(): MutableLiveData<Boolean> = isListViewMode

    fun getSortName(): MutableLiveData<String> = sortName

    fun getRecordToRelocate(): MutableLiveData<Record> = recordToRelocate

    fun getIsRelocationMode(): MutableLiveData<Boolean> = isRelocationMode

    fun getIsCreateAvailable(): Boolean = isCreateAvailable

    fun getRelocationType(): MutableLiveData<RelocationType> = relocationType

    fun getCurrentFolder(): MutableLiveData<NavigationFolder> = currentFolder

    fun getOnShowMessage(): MutableLiveData<String> = showMessage

    fun getOnShowQuotaExceeded(): SingleLiveEvent<Void> = showQuotaExceeded

    fun getOnChangeViewMode(): SingleLiveEvent<Boolean> = onChangeViewMode

    fun getOnCancelAllUploads(): SingleLiveEvent<Void> = onCancelAllUploads

    fun getOnDownloadsRetrieved(): MutableLiveData<MutableList<Download>> = onDownloadsRetrieved

    fun getOnDownloadFinished(): MutableLiveData<Download> = onDownloadFinished

    fun getOnRecordsRetrieved(): MutableLiveData<List<Record>> = onRecordsRetrieved

    fun getOnNewTemporaryFile(): MutableLiveData<Record> = onNewTemporaryFile

    fun getOnRecordDeleteRequest(): MutableLiveData<Record> = onRecordDeleteRequest

    fun getOnFileViewRequest(): MutableLiveData<ArrayList<Record>> = onFileViewRequest

    fun getOnPhotoSelected(): MutableLiveData<Record> = onPhotoSelected

    fun getOnShowSortOptionsFragment(): MutableLiveData<SortType> = onShowSortOptionsFragment

    fun getOnShowRecordSearchFragment(): MutableLiveData<Void> = onShowRecordSearchFragment

    fun getOnShowAddOptionsFragment(): MutableLiveData<NavigationFolderIdentifier> =
        onShowAddOptionsFragment

    fun getOnShowRecordOptionsFragment(): MutableLiveData<Record> = onShowRecordOptionsFragment

    fun getShowScreenSimplified(): MutableLiveData<Boolean> = showScreenSimplified

    companion object {
        const val MILLIS_UNTIL_REFRESH = 9000L
        const val MOBILE_UPLOADS_FOLDER_NAME = "Mobile Uploads"
    }
}
