package org.permanent.permanent.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
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
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.Constants
import org.permanent.permanent.CurrentArchivePermissionsManager
import org.permanent.permanent.FeatureFlags
import org.permanent.permanent.R
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Archive
import org.permanent.permanent.models.Download
import org.permanent.permanent.models.NavigationFolder
import org.permanent.permanent.models.NavigationFolderIdentifier
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.models.Upload
import org.permanent.permanent.network.StelaAuthState
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.myFiles.CancelListener
import org.permanent.permanent.ui.myFiles.FolderChildrenPager
import org.permanent.permanent.ui.myFiles.PagedFolderChildren
import org.permanent.permanent.ui.myFiles.listingFailureMessage
import org.permanent.permanent.ui.myFiles.ListFooter
import org.permanent.permanent.ui.myFiles.ModificationType
import org.permanent.permanent.ui.myFiles.OnFinishedListener
import org.permanent.permanent.ui.myFiles.SortType
import org.permanent.permanent.ui.myFiles.download.DownloadQueue
import org.permanent.permanent.ui.myFiles.upload.UploadsAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Stack

class SharedXMeViewModel(application: Application) : SelectionViewModel(application),
    CancelListener, OnFinishedListener {

    private val TAG = SharedXMeViewModel::class.java.simpleName
    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private lateinit var lifecycleOwner: LifecycleOwner
    private var refreshJob: Job? = null

    // Tab discriminator: by-me stamps the session archive role; with-me uses the
    // payload accessRole.
    private var isSharedByMe = false

    private var selectAllWhenListed = false
    private val pager by lazy {
        PagedFolderChildren(fileRepository, prepareRecords = ::stampSharesFields) {
            loadFilesOf(currentFolder.value, currentSortType.value, isRefresh = true, wholeFolder = true)
        }
    }

    val isRoot = MutableLiveData(true)
    private val isCreateAvailable = MutableLiveData(true)
    private val isListViewMode = MutableLiveData(prefsHelper.isListViewMode())
    private var existsDownloads = MutableLiveData(false)
    private val folderName = MutableLiveData(Constants.MY_FILES_FOLDER)
    private val currentSortType: MutableLiveData<SortType> =
        MutableLiveData(SortType.NAME_ASCENDING)
    private val sortName: MutableLiveData<String> =
        MutableLiveData(SortType.NAME_ASCENDING.toUIString())
    private val sortLabel = MutableLiveData(SortType.NAME_ASCENDING.toLabel(appContext))
    val usesPagedList get() = FeatureFlags.useStelaMigration
    private var folderPathStack: Stack<Record> = Stack()

    private val showQuotaExceeded = SingleLiveEvent<Void?>()
    private val onShowAddOptionsFragment = SingleLiveEvent<NavigationFolderIdentifier>()
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
        applySortType(sortType)
        val folder = currentFolder.value
        val folderLinkId = folder?.getFolderIdentifier()?.folderLinkId
        // Saving needs edit rights on the shared folder itself.
        if (!usesPagedList || folder == null || folderLinkId == null ||
            folder.getAccessRole()?.isEditAvailable() != true
        ) {
            loadFilesOf(folder, sortType)
            return
        }
        showFirstPageSkeleton()
        fileRepository.saveFolderSort(
            folder.getFolderIdentifier()?.folderId, folderLinkId, folder.getArchiveId(), sortType,
            object : IResponseListener {
                override fun onSuccess(message: String?) {
                    if (currentFolder.value !== folder || currentSortType.value != sortType) return
                    folder.setSavedSort(sortType)
                    loadFilesOf(folder, sortType)
                }

                // Not saved: the folder is listed whole and sorted on the device.
                override fun onFailed(error: String?) {
                    if (currentFolder.value !== folder || currentSortType.value != sortType) return
                    loadFilesOf(folder, sortType)
                }
            })
    }

    private fun applySortType(sortType: SortType) {
        currentSortType.value = sortType
        sortName.value = sortType.toUIString()
        sortLabel.value = sortType.toLabel(appContext)
    }

    fun setShowScreenSimplified() {
        showScreenSimplified.value = true
    }

    fun setIsSharedByMe(isSharedByMe: Boolean) {
        this.isSharedByMe = isSharedByMe
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
            // Entering a folder lists it in its saved sort.
            if (usesPagedList) record.savedSort?.let { applySortType(it) }
            isCreateAvailable.value =
                record.accessRole != AccessRole.VIEWER && CurrentArchivePermissionsManager.instance.isCreateAvailable()
            loadEnqueuedUploads(currentFolder.value, lifecycleOwner)
            loadFilesOf(currentFolder.value, currentSortType.value, forwardNavigation = true)
        } else {
            if (showScreenSimplified.value == false) {
                onFileViewRequest.value = record
            }
        }
    }

    fun onBackBtnClick() {
        if (isRelocationMode.value == true && folderPathStack.size == 1) { // There is only the root
            showRelocationCancellationDialog.call()
        } else {
            navigateBack()
        }
    }

    internal fun navigateBack() {
        if (folderPathStack.isEmpty()) return
        currentFolder.value?.getUploadQueue()?.clearEnqueuedUploadsAndRemoveTheirObservers()
        // Popping the record of the current folder
        folderPathStack.pop()
        if (folderPathStack.isEmpty()) {
            if (usesPagedList) pager.reset()
            onRootSharesNeeded.call()
        } else {
            val previousFolder = folderPathStack.peek()
            currentFolder.value = NavigationFolder(appContext, previousFolder)
            if (usesPagedList) previousFolder.savedSort?.let { applySortType(it) }
            isCreateAvailable.value =
                previousFolder.accessRole != AccessRole.VIEWER && CurrentArchivePermissionsManager.instance.isCreateAvailable()
            loadEnqueuedUploads(currentFolder.value, lifecycleOwner)
            loadFilesOf(currentFolder.value, currentSortType.value)
        }
    }

    private fun loadFilesOf(
        folder: NavigationFolder?,
        sortType: SortType?,
        forwardNavigation: Boolean = false,
        isRefresh: Boolean = false,
        wholeFolder: Boolean = false
    ) {
        val archiveNr = folder?.getArchiveNr()
        val folderLinkId = folder?.getFolderIdentifier()?.folderLinkId
        if (archiveNr != null && folderLinkId != null) {
            if (usesPagedList && !isRefresh) {
                showFirstPageSkeleton()
            } else {
                swipeRefreshLayout.isRefreshing = true
                if (usesPagedList) pager.startExternalListing(showsSkeleton = false)
            }
            // Stela V2 drill-in, both tabs: reads authorize server-side, share
            // membership included — no ownership condition. V1 is the automatic
            // failsafe.
            val folderId = folder.getFolderIdentifier()?.folderId
            if (StelaAuthState.isV2ReadEnabled && folderId != null && folderId > 0) {
                loadFilesOfV2(folder, sortType, forwardNavigation, isRefresh, wholeFolder)
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
                    if (usesPagedList) {
                        val records = recordVOs?.let { getRecords(it) } ?: mutableListOf()
                        commitListing(records, wholeFolder = true)
                    } else {
                        recordVOs?.let { onRecordsRetrieved.value = getRecords(recordVOs) }
                    }
                }

                override fun onFailed(error: String?) {
                    swipeRefreshLayout.isRefreshing = false
                    if (usesPagedList) pager.onListingFailed()
                    showMessage.value = listingFailureMessage(appContext)
                }
            })
    }

    // Every fetch completes exactly once: commit, V1 fallback, retry, or a quiet
    // spinner stop. Only the newest fetch may commit (see FolderChildrenPager).
    private fun loadFilesOfV2(
        folder: NavigationFolder,
        sortType: SortType?,
        forwardNavigation: Boolean,
        isRefresh: Boolean,
        forceWholeFolder: Boolean,
        retriesLeft: Int = 1
    ) {
        val folderId = folder.getFolderIdentifier()?.folderId ?: return
        // Server order is the folder's saved sort; any other sort needs the whole folder.
        val wholeFolder = forceWholeFolder || sortType == null || folder.getSavedSort() != sortType
        pager.list(folderId, wholeFolder, isRefresh,
            object : FolderChildrenPager.ListingListener {

                override fun onSuccess(records: List<Record>) {
                    swipeRefreshLayout.isRefreshing = false
                    if (BuildConfig.DEBUG) Log.d(TAG, "Children of folder $folderId served by V2")
                    applyFolderHeader(folder)
                    val listed = records.toMutableList()
                    if (wholeFolder && sortType != null) listed.sortWith(sortType.toComparator())
                    stampSharesFields(listed)
                    existsFiles.value = listed.isNotEmpty()
                    commitListing(listed)
                }

                override fun onFailed(error: String?) {
                    if (BuildConfig.DEBUG) Log.d(
                        TAG, "V2 children of folder $folderId failed ($error), falling back to V1"
                    )
                    loadFilesOfV1(folder, sortType)
                }

                override fun onSuperseded() {
                    if (forwardNavigation && retriesLeft > 0 && currentFolder.value == folder) {
                        // A refresh raced the user's tap — retry once so the tap is never eaten.
                        loadFilesOfV2(
                            folder, sortType, forwardNavigation, isRefresh, forceWholeFolder,
                            retriesLeft - 1
                        )
                    } else {
                        // The superseding fetch repaints this folder — only the spinner must stop.
                        swipeRefreshLayout.isRefreshing = false
                    }
                }
            })
    }

    // By-me overrides the payload role with the session archive's — retained shipped
    // behavior, equivalent for own-archive content; with-me keeps the mapper's role.
    private fun stampSharesFields(records: List<Record>) {
        val sessionAccessRole = CurrentArchivePermissionsManager.instance.getAccessRole()
        records.forEach {
            it.displayInShares = true
            if (isSharedByMe) it.accessRole = sessionAccessRole
        }
    }

    private fun showFirstPageSkeleton() {
        swipeRefreshLayout.isRefreshing = false
        pager.startExternalListing(showsSkeleton = true)
        // Keeps the sort and Select row visible over the skeleton.
        existsFiles.value = true
        onRecordsRetrieved.value = mutableListOf()
    }

    private fun commitListing(records: MutableList<Record>, wholeFolder: Boolean = false) {
        if (wholeFolder) pager.commitWholeFolder(records) else pager.commit(records)
        onRecordsRetrieved.value = records
        if (selectAllWhenListed) {
            selectAllWhenListed = false
            super.onSelectAllRecords(pager.records)
        }
    }

    fun onListEndReached() = pager.loadNextPage()

    fun onRetryNextPageClick() = pager.retryNextPage()

    private fun applyFolderHeader(folder: NavigationFolder) {
        isRoot.value = false
        folderName.value = folder.getDisplayName()
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
            loadFilesOf(currentFolder.value, currentSortType.value, isRefresh = true)
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
                        message?.let { showMessage.value = it }
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

    fun onSelectAllBtnClick() {
        if (!usesPagedList) {
            super.onSelectAllRecords(onRecordsRetrieved.value!!)
        } else if (pager.isComplete) {
            super.onSelectAllRecords(pager.records)
        } else {
            // Select all covers the whole folder, so the missing pages load first.
            selectAllWhenListed = true
            loadFilesOf(currentFolder.value, currentSortType.value, isRefresh = true, wholeFolder = true)
        }
    }

    override fun onRecordDeleteClick(record: Record) {}

    fun getIsListViewMode(): MutableLiveData<Boolean> = isListViewMode

    fun getExistsUploads(): MutableLiveData<Boolean> = uploadsAdapter.getExistsUploads()

    fun getExistsDownloads(): MutableLiveData<Boolean> = existsDownloads

    fun getFolderName(): MutableLiveData<String> = folderName

    fun getSortName(): MutableLiveData<String> = sortName

    fun getSortLabel(): MutableLiveData<String> = sortLabel

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

    fun getOnRecordsAppended(): LiveData<List<Record>> = pager.appended

    fun getListFooter(): LiveData<ListFooter> = pager.footer

    fun getOnRootSharesNeeded(): LiveData<Void?> = onRootSharesNeeded

    fun getOnChangeViewMode(): MutableLiveData<Boolean> = onChangeViewMode

    fun getOnRecordSelected(): MutableLiveData<Record> = onRecordSelected

    fun getShowScreenSimplified(): MutableLiveData<Boolean> = showScreenSimplified

    fun getOnFileViewRequest(): LiveData<Record> = onFileViewRequest

    fun getShowRelocationCancellationDialog(): LiveData<Void?> = showRelocationCancellationDialog

    fun getOnShowSortOptionsFragment(): MutableLiveData<SortType> = onShowSortOptionsFragment

    fun getIsRelocationMode(): MutableLiveData<Boolean> = isRelocationMode

    fun getIsSelectionMode(): MutableLiveData<Boolean> = isSelectionMode

    fun getOpenChecklistBottomSheet(): MutableLiveData<Void?> = openChecklistBottomSheet

    fun getCurrentArchive() : Archive = prefsHelper.getCurrentArchive()
}
