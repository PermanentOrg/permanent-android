package org.permanent.permanent.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.net.Uri
import android.text.Editable
import android.util.Log
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import org.permanent.permanent.ArchivePermissionsManager
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.models.*
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.network.IRecordListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.repositories.INotificationRepository
import org.permanent.permanent.repositories.NotificationRepositoryImpl
import org.permanent.permanent.ui.myFiles.*
import org.permanent.permanent.ui.myFiles.download.DownloadQueue
import org.permanent.permanent.ui.myFiles.upload.UploadsAdapter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MyFilesViewModel(application: Application) : ObservableAndroidViewModel(application),
    RecordListener, RecordOptionsClickListener, CancelListener, OnFinishedListener {

    private val TAG = MyFilesViewModel::class.java.simpleName
    private val appContext = application.applicationContext
    private val folderName = MutableLiveData(Constants.MY_FILES_FOLDER)
    private val isRoot = MutableLiveData(true)
    private val isSortedAsc = MutableLiveData(true)
    private val sortName: MutableLiveData<String> =
        MutableLiveData(SortType.NAME_ASCENDING.toUIString())
    private val isListViewMode = MutableLiveData(true)
    private val isCreateAvailable = ArchivePermissionsManager.instance.isCreateAvailable()
    private val isRelocationMode = MutableLiveData(false)
    private val relocationType = MutableLiveData<RelocationType>()
    private val currentSortType: MutableLiveData<SortType> =
        MutableLiveData(SortType.NAME_ASCENDING)
    private val currentSearchQuery = MutableLiveData<String>()
    private var currentFolder = MutableLiveData<NavigationFolder>()
    private val existsFiles = MutableLiveData(false)
    private var existsDownloads = MutableLiveData(false)
    private val recordToRelocate = MutableLiveData<Record>()
    private val showMessage = SingleLiveEvent<String>()
    private val showQuotaExceeded = SingleLiveEvent<Void>()
    private val onChangeViewMode = SingleLiveEvent<Boolean>()
    private val onCancelAllUploads = SingleLiveEvent<Void>()
    private val onDownloadsRetrieved = SingleLiveEvent<MutableList<Download>>()
    private val onDownloadFinished = SingleLiveEvent<Download>()
    private val onRecordsRetrieved = SingleLiveEvent<List<Record>>()
    private val onFilesFilterQuery = MutableLiveData<Editable>()
    private val onNewTemporaryFile = SingleLiveEvent<Record>()
    private val onShowAddOptionsFragment = SingleLiveEvent<NavigationFolderIdentifier>()
    private val onShowRecordOptionsFragment = SingleLiveEvent<Record>()
    private val onShowSortOptionsFragment = SingleLiveEvent<SortType>()
    private val onRecordDeleteRequest = SingleLiveEvent<Record>()
    private val onFileViewRequest = SingleLiveEvent<ArrayList<Record>>()

    private var fileRepository: IFileRepository = FileRepositoryImpl(application)
    private var folderPathStack: Stack<Record> = Stack()
    private lateinit var uploadsAdapter: UploadsAdapter
    private lateinit var downloadQueue: DownloadQueue
    private lateinit var uploadsRecyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var fragmentManager: FragmentManager
    private lateinit var lifecycleOwner: LifecycleOwner

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

    fun populateMyFiles() {
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
                showMessage.value = error
            }
        })
    }

    fun setExistsDownloads(existsDownloads: MutableLiveData<Boolean>) {
        this.existsDownloads = existsDownloads
    }

    fun onSearchQueryTextChanged(query: Editable) {
        currentSearchQuery.value = query.toString()
        onFilesFilterQuery.value = query
    }

    fun refreshCurrentFolder() {
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
                        folderName.value = parentName
                        isRoot.value = parentName.equals(Constants.MY_FILES_FOLDER)
                        existsFiles.value = !recordVOs.isNullOrEmpty()
                        recordVOs?.let { onRecordsRetrieved.value = getRecords(recordVOs) }
                    }

                    override fun onFailed(error: String?) {
                        swipeRefreshLayout.isRefreshing = false
                        showMessage.value = error
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

        if (record.type == RecordType.FOLDER) {
            currentFolder.value?.getUploadQueue()?.clearEnqueuedUploadsAndRemoveTheirObservers()
            folderPathStack.push(record)
            loadFilesAndUploadsOf(record)
        } else {
            record.viewFirst = true
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

    fun onBackBtnClick() {
        currentFolder.value?.getUploadQueue()?.clearEnqueuedUploadsAndRemoveTheirObservers()
        // Popping the record of the current folder
        folderPathStack.pop()
        val previousFolder = folderPathStack.peek()
        loadFilesAndUploadsOf(previousFolder)
    }

    private fun loadFilesAndUploadsOf(record: Record) {
        currentFolder.value = NavigationFolder(appContext, record)
        loadEnqueuedUploads(currentFolder.value, lifecycleOwner)
        loadFilesOf(currentFolder.value, currentSortType.value)
    }

    private fun loadEnqueuedUploads(folder: NavigationFolder?, lifecycleOwner: LifecycleOwner) {
        folder?.newUploadQueue(lifecycleOwner, this)
            ?.getEnqueuedUploadsLiveData()?.let { enqueuedUploadsLiveData ->
                enqueuedUploadsLiveData.observe(lifecycleOwner, { enqueuedUploads ->
                    uploadsAdapter.set(enqueuedUploads)
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

    fun upload(uris: List<Uri>) {
        currentFolder.value?.getUploadQueue()?.upload(uris)
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

    override fun onCancelClick(download: Download) {
        download.cancel()
        downloadQueue.removeDownload(download)
    }

    override fun onFinished(upload: Upload, succeeded: Boolean) {
        currentFolder.value?.getUploadQueue()?.removeFinishedUpload(upload)
        uploadsAdapter.remove(upload)

        if (succeeded) addFakeItemToFilesList(upload)
        if (uploadsAdapter.itemCount == 0) {
            refreshCurrentFolder()
        }
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
        isSortedAsc.value = sortType == SortType.FILE_TYPE_ASCENDING
                || sortType == SortType.DATE_ASCENDING
                || sortType == SortType.NAME_ASCENDING
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
                        showMessage.value = message
                        onNewTemporaryFile.value = recordToRelocate.value
                        existsFiles.value = true
                    }

                    override fun onFailed(error: String?) {
                        swipeRefreshLayout.isRefreshing = false
                        showMessage.value = error
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
                showMessage.value = error
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

    fun getIsRoot(): MutableLiveData<Boolean> = isRoot

    fun getIsSortedAsc(): MutableLiveData<Boolean> = isSortedAsc

    fun getIsListViewMode(): MutableLiveData<Boolean> = isListViewMode

    fun getSortName(): MutableLiveData<String> = sortName

    fun getRecordToRelocate(): MutableLiveData<Record> = recordToRelocate

    fun getIsRelocationMode(): MutableLiveData<Boolean> = isRelocationMode

    fun getIsCreateAvailable(): Boolean = isCreateAvailable

    fun getRelocationType(): MutableLiveData<RelocationType> = relocationType

    fun getCurrentFolder(): MutableLiveData<NavigationFolder> = currentFolder

    fun getCurrentSearchQuery(): MutableLiveData<String> = currentSearchQuery

    fun getOnShowMessage(): MutableLiveData<String> = showMessage

    fun getOnShowQuotaExceeded(): SingleLiveEvent<Void> = showQuotaExceeded

    fun getOnChangeViewMode(): SingleLiveEvent<Boolean> = onChangeViewMode

    fun getOnCancelAllUploads(): SingleLiveEvent<Void> = onCancelAllUploads

    fun getOnDownloadsRetrieved(): MutableLiveData<MutableList<Download>> = onDownloadsRetrieved

    fun getOnDownloadFinished(): MutableLiveData<Download> = onDownloadFinished

    fun getOnRecordsRetrieved(): MutableLiveData<List<Record>> = onRecordsRetrieved

    fun getOnFilesFilterQuery(): MutableLiveData<Editable> = onFilesFilterQuery

    fun getOnNewTemporaryFile(): MutableLiveData<Record> = onNewTemporaryFile

    fun getOnRecordDeleteRequest(): MutableLiveData<Record> = onRecordDeleteRequest

    fun getOnFileViewRequest(): MutableLiveData<ArrayList<Record>> = onFileViewRequest

    fun getOnShowSortOptionsFragment(): MutableLiveData<SortType> = onShowSortOptionsFragment

    fun getOnShowAddOptionsFragment(): MutableLiveData<NavigationFolderIdentifier> =
        onShowAddOptionsFragment

    fun getOnShowRecordOptionsFragment(): MutableLiveData<Record> = onShowRecordOptionsFragment
}
