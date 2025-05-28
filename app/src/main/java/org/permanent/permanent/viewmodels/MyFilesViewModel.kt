package org.permanent.permanent.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
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
import org.permanent.permanent.PermanentApplication
import org.permanent.permanent.R
import org.permanent.permanent.models.Account
import org.permanent.permanent.models.AccountEventAction
import org.permanent.permanent.models.Download
import org.permanent.permanent.models.EventAction
import org.permanent.permanent.models.NavigationFolder
import org.permanent.permanent.models.NavigationFolderIdentifier
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordEventAction
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.models.Upload
import org.permanent.permanent.network.IRecordListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.repositories.AccountRepositoryImpl
import org.permanent.permanent.repositories.EventsRepositoryImpl
import org.permanent.permanent.repositories.IAccountRepository
import org.permanent.permanent.repositories.IEventsRepository
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.repositories.INotificationRepository
import org.permanent.permanent.repositories.NotificationRepositoryImpl
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

open class MyFilesViewModel(application: Application) : SelectionViewModel(application),
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
    private val currentSortType: MutableLiveData<SortType> =
        MutableLiveData(SortType.NAME_ASCENDING)
    private var existsDownloads = MutableLiveData(false)
    private val showQuotaExceeded = SingleLiveEvent<Void?>()
    private val onChangeViewMode = SingleLiveEvent<Boolean>()
    private val onCancelAllUploads = SingleLiveEvent<Void?>()
    private val onDownloadsRetrieved = SingleLiveEvent<MutableList<Download>>()
    private val onDownloadFinished = SingleLiveEvent<Download>()
    private val onRecordsRetrieved = SingleLiveEvent<List<Record>>()
    private val onShowRecordSearchFragment = SingleLiveEvent<Void?>()
    private val onShowAddOptionsFragment = SingleLiveEvent<NavigationFolderIdentifier>()
    private val onShowRecordOptionsFragment = SingleLiveEvent<Record>()
    private val onShowSortOptionsFragment = SingleLiveEvent<SortType>()
    private val onRecordDeleteRequest = SingleLiveEvent<Record>()
    private val onFileViewRequest = SingleLiveEvent<ArrayList<Record>>()
    private val onRecordSelected = SingleLiveEvent<Record>()
    private val openChecklistBottomSheet = SingleLiveEvent<Void?>()
    private var showScreenSimplified = MutableLiveData(false)
    private val showChecklistFab = MutableLiveData(false)

    protected var accountRepository: IAccountRepository = AccountRepositoryImpl(application)
    private var eventsRepository: IEventsRepository = EventsRepositoryImpl(application)
    protected var folderPathStack: Stack<Record> = Stack()
    private lateinit var uploadsAdapter: UploadsAdapter
    private lateinit var downloadQueue: DownloadQueue
    private lateinit var uploadsRecyclerView: RecyclerView
    protected lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var fragmentManager: FragmentManager
    protected lateinit var lifecycleOwner: LifecycleOwner
    private val prefsHelper = PreferencesHelper(
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    init {
        PermanentApplication.instance.relocateData?.let {
            setRelocationMode(it)
        }
    }

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

    fun getHideChecklist() {
        swipeRefreshLayout.isRefreshing = true
        accountRepository.getAccount(object : IAccountRepository.IAccountListener {

            override fun onSuccess(account: Account) {
                swipeRefreshLayout.isRefreshing = false
                showChecklistFab.value = account.hideChecklist != null && !account.hideChecklist!!
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
            fileRepository.getChildRecordsOf(archiveNr,
                folderLinkId,
                sortType?.toBackendString(),
                object : IFileRepository.IOnRecordsRetrievedListener {

                    override fun onSuccess(parentFolderName: String?, recordVOs: List<RecordVO>?) {
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

    fun onChecklistFabClick() {
        openChecklistBottomSheet.call()
    }

    override fun onRecordOptionsClick(record: Record) {
        onShowRecordOptionsFragment.value = record
    }

    override fun onRecordCheckBoxClick(record: Record) {
        super.onRecordChecked(record)
    }

    fun onSelectAllBtnClick() {
        super.onSelectAllRecords(onRecordsRetrieved.value!!)
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

        if (showScreenSimplified.value == true) onRecordSelected.value = record

        when (record.type) {
            RecordType.FOLDER -> {
                currentFolder.value?.getUploadQueue()?.clearEnqueuedUploadsAndRemoveTheirObservers()
                folderPathStack.push(record)
                loadFilesAndUploadsOf(record)
            }
            else -> {
                if (showScreenSimplified.value == false) {
                    record.displayFirstInCarousel = true
                    onFileViewRequest.value = getFilesForViewing(onRecordsRetrieved.value)
                }
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
        folder?.newUploadQueue(lifecycleOwner, this)?.getEnqueuedUploadsLiveData()
            ?.let { enqueuedUploadsLiveData ->
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
        folderName.value?.let { sendEvent(AccountEventAction.INITIATE_UPLOAD, data = mapOf("workspace" to it)) }
        folder.getUploadQueue()?.upload(uris)
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

        if (succeeded) {
            addFakeItemToFilesList(upload)
            folderName.value?.let { sendEvent(RecordEventAction.SUBMIT, data = mapOf("workspace" to it)) }
        }
        if (uploadsAdapter.itemCount == 0) {
            existsFiles.value = true
            refreshJob?.cancel()
            refreshJob = viewModelScope.launch {
                delay(MILLIS_UNTIL_REFRESH_AFTER_UPLOAD)
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
    }

    fun setSortType(sortType: SortType) {
        currentSortType.value = sortType
        sortName.value = sortType.toUIString()
        loadFilesOf(currentFolder.value, currentSortType.value)
    }

    fun delete(record: Record) {
        swipeRefreshLayout.isRefreshing = true
        fileRepository.deleteRecords(mutableListOf(record), object : IResponseListener {
            override fun onSuccess(message: String?) {
                swipeRefreshLayout.isRefreshing = false
                refreshCurrentFolder()
                if (record.type == RecordType.FOLDER) showMessage.value =
                    appContext.getString(R.string.my_files_folder_deleted)
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

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e(TAG, "Fetching FCM token failed: ${task.exception}")
                return@OnCompleteListener
            }
            notificationsRepository.registerDevice(task.result, object : IResponseListener {

                override fun onSuccess(message: String?) {
                }

                override fun onFailed(error: String?) {
                    Log.e(TAG, "Registering Device FCM token failed: $error")
                }
            })
        })
    }

    fun sendEvent(action: EventAction, data: Map<String, String> = mapOf()) {
        eventsRepository.sendEventAction(
            eventAction = action,
            accountId = prefsHelper.getAccountId(),
            data = data
        )
    }

    fun getFolderName(): MutableLiveData<String> = folderName

    fun getExistsDownloads(): MutableLiveData<Boolean> = existsDownloads

    fun getExistsUploads(): MutableLiveData<Boolean> = uploadsAdapter.getExistsUploads()

    fun getIsRoot(): MutableLiveData<Boolean> = isRoot

    fun getIsListViewMode(): MutableLiveData<Boolean> = isListViewMode

    fun getSortName(): MutableLiveData<String> = sortName

    fun getIsRelocationMode(): MutableLiveData<Boolean> = isRelocationMode

    fun getIsSelectionMode(): MutableLiveData<Boolean> = isSelectionMode

    fun getIsCreateAvailable(): Boolean = isCreateAvailable

    fun getOnShowMessage(): MutableLiveData<String> = showMessage

    fun getOnShowQuotaExceeded(): SingleLiveEvent<Void?> = showQuotaExceeded

    fun getOnChangeViewMode(): SingleLiveEvent<Boolean> = onChangeViewMode

    fun getOnCancelAllUploads(): SingleLiveEvent<Void?> = onCancelAllUploads

    fun getOnDownloadsRetrieved(): MutableLiveData<MutableList<Download>> = onDownloadsRetrieved

    fun getOnDownloadFinished(): MutableLiveData<Download> = onDownloadFinished

    fun getOnRecordsRetrieved(): MutableLiveData<List<Record>> = onRecordsRetrieved

    fun getOnNewTemporaryFiles(): MutableLiveData<MutableList<Record>> = onNewTemporaryFiles

    fun getOnRecordDeleteRequest(): MutableLiveData<Record> = onRecordDeleteRequest

    fun getOnFileViewRequest(): MutableLiveData<ArrayList<Record>> = onFileViewRequest

    fun getOnRecordSelected(): MutableLiveData<Record> = onRecordSelected

    fun getOnShowSortOptionsFragment(): MutableLiveData<SortType> = onShowSortOptionsFragment

    fun getOnShowRecordSearchFragment(): MutableLiveData<Void?> = onShowRecordSearchFragment

    fun getOpenChecklistBottomSheet(): MutableLiveData<Void?> = openChecklistBottomSheet

    fun getOnShowAddOptionsFragment(): MutableLiveData<NavigationFolderIdentifier> =
        onShowAddOptionsFragment

    fun getOnShowRecordOptionsFragment(): MutableLiveData<Record> = onShowRecordOptionsFragment

    fun getShowScreenSimplified(): MutableLiveData<Boolean> = showScreenSimplified

    fun getShowChecklistFab(): MutableLiveData<Boolean> = showChecklistFab

    companion object {
        const val MILLIS_UNTIL_REFRESH_AFTER_UPLOAD = 9000L
        const val MILLIS_UNTIL_REFRESH_AFTER_DELETE = 1000L
    }
}
