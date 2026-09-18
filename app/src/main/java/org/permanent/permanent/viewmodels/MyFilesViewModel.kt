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
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.Constants
import org.permanent.permanent.CurrentArchivePermissionsManager
import org.permanent.permanent.FeatureFlags
import org.permanent.permanent.PermanentApplication
import org.permanent.permanent.R
import org.permanent.permanent.models.AccountEventAction
import org.permanent.permanent.models.Archive
import org.permanent.permanent.models.Download
import org.permanent.permanent.models.EventAction
import org.permanent.permanent.models.NavigationFolder
import org.permanent.permanent.models.NavigationFolderIdentifier
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordEventAction
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.models.Upload
import org.permanent.permanent.network.IRecordListener
import org.permanent.permanent.network.StelaAuthState
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.IFolderChildrenListener
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.repositories.EventsRepositoryImpl
import org.permanent.permanent.repositories.IEventsRepository
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.repositories.INotificationRepository
import org.permanent.permanent.repositories.NotificationRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.myFiles.CancelListener
import org.permanent.permanent.ui.myFiles.ModificationType
import org.permanent.permanent.ui.myFiles.OnFinishedListener
import org.permanent.permanent.ui.myFiles.SortType
import org.permanent.permanent.ui.myFiles.download.DownloadQueue
import org.permanent.permanent.ui.myFiles.upload.UploadsAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Stack

open class MyFilesViewModel(application: Application) : SelectionViewModel(application),
    CancelListener, OnFinishedListener {

    private val TAG = MyFilesViewModel::class.java.simpleName
    private val appContext = application.applicationContext
    private val folderName = MutableLiveData(Constants.PRIVATE_FILES)
    private var refreshJob: Job? = null

    // Monotonic id of the newest V2 children fetch; only the newest may commit and
    // superseded fetches complete quietly (see loadFilesOfV2). Touched on main only.
    private var childrenFetchGeneration = 0

    // Same guard for root loads (see loadRootFilesV2). Touched on main only.
    private var rootLoadGeneration = 0

    // Cleared only in rootLoadListener. Touched on main only.
    private var isRootLoadInFlight = false
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
    private val onShowSortOptionsFragment = SingleLiveEvent<SortType>()
    private val onRecordDeleteRequest = SingleLiveEvent<Record>()
    private val onFileViewRequest = SingleLiveEvent<ArrayList<Record>>()
    private val onRecordSelected = SingleLiveEvent<Record>()
    private val openChecklistBottomSheet = SingleLiveEvent<Void?>()
    private var showScreenSimplified = MutableLiveData(false)

    private var eventsRepository: IEventsRepository = EventsRepositoryImpl(application)
    protected var folderPathStack: Stack<Record> = Stack()
    private lateinit var uploadsAdapter: UploadsAdapter
    private lateinit var downloadQueue: DownloadQueue
    private lateinit var uploadsRecyclerView: RecyclerView
    protected lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var fragmentManager: FragmentManager
    protected lateinit var lifecycleOwner: LifecycleOwner
    protected val prefsHelper = PreferencesHelper(
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
        if (FeatureFlags.useStelaMigration) {
            loadRootFilesV2()
        } else {
            loadRootFilesV1()
        }
    }

    protected open fun loadRootFilesV1() {
        swipeRefreshLayout.isRefreshing = true
        fileRepository.getMyFilesRecord(object : IRecordListener {
            override fun onSuccess(record: Record) {
                swipeRefreshLayout.isRefreshing = false
                folderPathStack.push(record)
                loadFilesAndUploadsOf(record, forwardNavigation = true)
                loadEnqueuedDownloads(lifecycleOwner)
            }

            override fun onFailed(error: String?) {
                swipeRefreshLayout.isRefreshing = false
                error?.let { showMessage.value = it }
            }
        })
    }

    // Only the newest root load may commit — the archive-changed observer re-fires
    // this on a live ViewModel. Subclasses supply the two resolver legs.
    private fun loadRootFilesV2() {
        swipeRefreshLayout.isRefreshing = true
        isRootLoadInFlight = true
        val generation = ++rootLoadGeneration
        val isStale = { generation != rootLoadGeneration }
        resolveRootV2(isStale, rootLoadListener(generation, isFinalAttempt = false) { error ->
            if (BuildConfig.DEBUG) Log.d(
                TAG, "V2 root resolution failed ($error), falling back to the V1 failsafe"
            )
            resolveRootFailsafe(rootLoadListener(generation) { fallbackError ->
                swipeRefreshLayout.isRefreshing = false
                fallbackError?.let { showMessage.value = it }
            })
        })
    }

    protected open fun resolveRootV2(isStale: () -> Boolean, listener: IRecordListener) {
        fileRepository.getMyFilesRecordV2(isStale, listener)
    }

    protected open fun resolveRootFailsafe(listener: IRecordListener) {
        fileRepository.getMyFilesRecord(listener)
    }

    // A superseded load neither commits nor runs its failsafe; the in-flight flag
    // clears on success and on a final attempt's failure.
    private fun rootLoadListener(
        generation: Int, isFinalAttempt: Boolean = true, onFailure: (String?) -> Unit
    ) = object : IRecordListener {
        override fun onSuccess(record: Record) {
            if (generation != rootLoadGeneration) return
            isRootLoadInFlight = false
            commitRootRecord(record)
        }

        override fun onFailed(error: String?) {
            if (generation != rootLoadGeneration) return
            if (isFinalAttempt) isRootLoadInFlight = false
            onFailure(error)
        }
    }

    protected open fun commitRootRecord(record: Record) {
        swipeRefreshLayout.isRefreshing = false
        // Reset instead of push: a root load on a surviving ViewModel (archive switch
        // from the Save-to-Permanent sheet) must not leave the previous archive's root
        // reachable through back navigation.
        folderPathStack.clear()
        folderPathStack.push(record)
        loadFilesAndUploadsOf(record, forwardNavigation = true)
        loadEnqueuedDownloads(lifecycleOwner)
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
        // A root load in flight repaints this screen anyway.
        if (isRootLoadInFlight) return
        refreshJob?.cancel()
        loadFilesOf(currentFolder.value, currentSortType.value)
    }

    private fun loadFilesOf(
        folder: NavigationFolder?,
        sortType: SortType?,
        forwardNavigation: Boolean = false
    ) {
        val archiveNr = folder?.getArchiveNr()
        val folderLinkId = folder?.getFolderIdentifier()?.folderLinkId
        if (archiveNr != null && folderLinkId != null) {
            swipeRefreshLayout.isRefreshing = true
            // Private Files (VSP-1778) and Public Files (via PublicFilesViewModel,
            // VSP-1808) take the Stela V2 children endpoint when the migration flag
            // is on, with V1 as an automatic failsafe.
            val folderId = folder.getFolderIdentifier()?.folderId
            if (StelaAuthState.isV2ReadEnabled && folderId != null && folderId > 0) {
                loadFilesOfV2(folder, sortType, forwardNavigation)
            } else {
                loadFilesOfV1(folder, sortType)
            }
        } else {
            // The folder can't be listed without its V1 address — report it instead
            // of silently dropping the navigation with the spinner left running.
            if (BuildConfig.DEBUG) Log.w(
                TAG,
                "Navigation dropped: folder missing V1 ids (archiveNr=$archiveNr, folderLinkId=$folderLinkId)"
            )
            swipeRefreshLayout.isRefreshing = false
            showMessage.value = appContext.getString(R.string.generic_error)
        }
    }

    private fun loadFilesOfV1(folder: NavigationFolder, sortType: SortType?) {
        // Callers (loadFilesOf and the V2 failsafe) have already checked these.
        val archiveNr = folder.getArchiveNr() ?: return
        val folderLinkId = folder.getFolderIdentifier()?.folderLinkId ?: return
        fileRepository.getChildRecordsOf(archiveNr,
            folderLinkId,
            sortType?.toBackendString(),
            object : IFileRepository.IOnRecordsRetrievedListener {

                override fun onSuccess(parentFolderName: String?, recordVOs: List<RecordVO>?) {
                    swipeRefreshLayout.isRefreshing = false
                    applyFolderHeader(folder)
                    existsFiles.value = !recordVOs.isNullOrEmpty()
                    recordVOs?.let { onRecordsRetrieved.value = getRecords(recordVOs) }
                }

                override fun onFailed(error: String?) {
                    swipeRefreshLayout.isRefreshing = false
                    error?.let { showMessage.value = it }
                }
            })
    }

    // Stela V2 navigation (VSP-1778). Every fetch completes exactly once: it either
    // commits, falls back to V1, retries, or ends the refresh spinner quietly.
    // The generation guard makes sure only the NEWEST fetch may commit — two loads
    // can be in flight (e.g. a post-upload refresh racing a folder tap) and land out
    // of order, which on the V1 path silently lets the last response win.
    private fun loadFilesOfV2(
        folder: NavigationFolder,
        sortType: SortType?,
        forwardNavigation: Boolean,
        retriesLeft: Int = 1
    ) {
        val folderId = folder.getFolderIdentifier()?.folderId ?: return
        val generation = ++childrenFetchGeneration
        fileRepository.getChildRecordsOfV2(folderId, object : IFolderChildrenListener {

            override fun onSuccess(records: List<Record>) {
                if (generation != childrenFetchGeneration) {
                    onFetchSuperseded(folder, sortType, forwardNavigation, retriesLeft)
                    return
                }
                swipeRefreshLayout.isRefreshing = false
                if (BuildConfig.DEBUG) Log.d(TAG, "Children of folder $folderId served by V2")
                applyFolderHeader(folder)
                // The endpoint has no sort param (sort is a folder attribute) —
                // apply the active sort locally, like iOS.
                val sortedRecords =
                    sortType?.let { records.sortedWith(it.toComparator()) } ?: records
                existsFiles.value = sortedRecords.isNotEmpty()
                onRecordsRetrieved.value = sortedRecords
            }

            override fun onFailed(error: String?) {
                if (generation != childrenFetchGeneration) {
                    // A superseded fetch must NEVER run the V1 failsafe — its
                    // out-of-order response could overwrite the newer listing.
                    onFetchSuperseded(folder, sortType, forwardNavigation, retriesLeft)
                    return
                }
                // V1 failsafe: this fetch is the newest, so there is no ordering hazard.
                if (BuildConfig.DEBUG) Log.d(
                    TAG, "V2 children of folder $folderId failed ($error), falling back to V1"
                )
                loadFilesOfV1(folder, sortType)
            }
        })
    }

    private fun onFetchSuperseded(
        folder: NavigationFolder,
        sortType: SortType?,
        forwardNavigation: Boolean,
        retriesLeft: Int
    ) {
        if (forwardNavigation && retriesLeft > 0 && currentFolder.value == folder) {
            // A background refresh raced the user's tap — retry once (claiming the
            // newest generation) so the tap is never eaten. Skipped when the user
            // has already navigated elsewhere.
            loadFilesOfV2(folder, sortType, forwardNavigation, retriesLeft - 1)
        } else {
            // Complete quietly, touching no data — the superseding fetch repaints
            // this folder anyway; only the spinner needs to terminate.
            swipeRefreshLayout.isRefreshing = false
        }
    }

    // isRoot/folderName derive from the locally navigated folder (not the server
    // response) — shared by the V1 and V2 paths.
    private fun applyFolderHeader(folder: NavigationFolder) {
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
                loadFilesAndUploadsOf(record, forwardNavigation = true)
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

    protected fun loadFilesAndUploadsOf(record: Record, forwardNavigation: Boolean = false) {
        currentFolder.value = NavigationFolder(appContext, record)
        loadEnqueuedUploads(currentFolder.value, lifecycleOwner)
        loadFilesOf(currentFolder.value, currentSortType.value, forwardNavigation)
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

    fun publishRecord(record: Record) {
        val folderLinkId = prefsHelper.getPublicRecordFolderLinkId()

        if (folderLinkId != 0) {
            swipeRefreshLayout.isRefreshing = true
            fileRepository.relocateRecords(mutableListOf(record),
                folderLinkId,
                prefsHelper.getPublicRecordFolderId(),
                ModificationType.PUBLISH,
                object : IResponseListener {
                    override fun onSuccess(message: String?) {
                        swipeRefreshLayout.isRefreshing = false
                        showMessage.value = message ?: appContext.getString(R.string.publish_success)
                    }

                    override fun onFailed(error: String?) {
                        swipeRefreshLayout.isRefreshing = false
                        error?.let { showMessage.value = it }
                    }
                })
        }
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

    fun getShowScreenSimplified(): MutableLiveData<Boolean> = showScreenSimplified

    fun getCurrentArchive() : Archive = prefsHelper.getCurrentArchive()

    companion object {
        const val MILLIS_UNTIL_REFRESH_AFTER_UPLOAD = 9000L
        const val MILLIS_UNTIL_REFRESH_AFTER_DELETE = 1000L
    }
}
