package org.permanent.permanent.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.FeatureFlags
import org.permanent.permanent.R
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.network.StelaAuthState
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.myFiles.FolderChildrenPager
import org.permanent.permanent.ui.myFiles.ListFooter
import org.permanent.permanent.ui.myFiles.PagedFolderChildren
import org.permanent.permanent.ui.myFiles.listingFailureMessage
import org.permanent.permanent.ui.myFiles.SortType
import java.util.*

class PublicFolderViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val TAG = PublicFolderViewModel::class.java.simpleName
    private var existsRecords = MutableLiveData(false)
    private val onFolderNameChanged = SingleLiveEvent<String>()
    private val isBusy = MutableLiveData(false)
    private val showMessage = SingleLiveEvent<String>()
    private var folderPathStack: Stack<Record> = Stack()
    private val onRecordsRetrieved = SingleLiveEvent<MutableList<Record>>()
    private val onFileViewRequest = SingleLiveEvent<ArrayList<Record>>()
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)
    val usesPagedList get() = FeatureFlags.useStelaMigration
    private var isListing = false
    private var listedArchiveNr: String? = null
    private val pager by lazy {
        PagedFolderChildren(fileRepository, prepareRecords = ::stampParentArchiveNr) {
            if (folderPathStack.isNotEmpty()) loadFilesOf(folderPathStack.peek(), wholeFolder = true)
        }
    }

    fun setRootFolder(rootFolder: Record?) {
        rootFolder?.let {
            folderPathStack.push(it)
            loadFilesOf(it)
        }
    }

    fun onRecordClick(record: Record) {
        if (record.type == RecordType.FOLDER) {
            folderPathStack.push(record)
            loadFilesOf(record)
        } else {
            record.displayFirstInCarousel = true
            onFileViewRequest.value =
                getFilesForViewing(if (usesPagedList) pager.records else onRecordsRetrieved.value)
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

    private fun loadFilesOf(record: Record, wholeFolder: Boolean = false) {
        if (isListing || isBusy.value == true) {
            return
        }
        val archiveNr = record.archiveNr
        val folderLinkId = record.folderLinkId
        if (archiveNr != null && folderLinkId != null) {
            // Public Gallery navigation (VSP-1810) takes the Stela V2 children endpoint
            // when the migration flag is on, with V1 as an automatic failsafe. A
            // deep-linked folder synthesized without a folderId falls through to V1 here.
            val folderId = record.folderId
            if (StelaAuthState.isV2ReadEnabled && folderId != null && folderId > 0) {
                loadFilesOfV2(record, folderId, archiveNr, folderLinkId, wholeFolder)
            } else {
                loadFilesOfV1(archiveNr, folderLinkId)
            }
        } else {
            // The folder can't be listed without its V1 address — report it instead
            // of silently ignoring the navigation.
            if (BuildConfig.DEBUG) Log.w(
                TAG,
                "Navigation dropped: record missing V1 ids (archiveNr=$archiveNr, folderLinkId=$folderLinkId)"
            )
            showMessage.value = getApplication<Application>().getString(R.string.generic_error)
        }
    }

    private fun loadFilesOfV1(archiveNr: String, folderLinkId: Int) {
        startListing(archiveNr)
        fileRepository.getChildRecordsOf(archiveNr,
            folderLinkId,
            SortType.NAME_ASCENDING?.toBackendString(),
            object : IFileRepository.IOnRecordsRetrievedListener {

                override fun onSuccess(parentFolderName: String?, recordVOs: List<RecordVO>?) {
                    endListing()
                    onFolderNameChanged.value = parentFolderName
                    existsRecords.value = !recordVOs.isNullOrEmpty()
                    if (usesPagedList) {
                        val records = recordVOs?.let { getRecords(it, archiveNr) } ?: mutableListOf()
                        pager.commitWholeFolder(records)
                        onRecordsRetrieved.value = records
                    } else {
                        recordVOs?.let {
                            onRecordsRetrieved.value = getRecords(recordVOs, archiveNr)
                        }
                    }
                }

                override fun onFailed(error: String?) {
                    endListing()
                    if (usesPagedList) pager.onListingFailed()
                    showMessage.value = listingFailureMessage(getApplication())
                }
            })
    }

    // Stela V2 navigation, page by page in the server's order. At most one listing is in
    // flight (the guard in loadFilesOf); a next page in flight is dropped by the pager.
    private fun loadFilesOfV2(
        record: Record, folderId: Int, archiveNr: String, folderLinkId: Int, wholeFolder: Boolean
    ) {
        startListing(archiveNr)
        pager.list(folderId, wholeFolder, isRefresh = false,
            object : FolderChildrenPager.ListingListener {

                override fun onSuccess(records: List<Record>) {
                    endListing()
                    if (BuildConfig.DEBUG) Log.d(TAG, "Children of folder $folderId served by V2")
                    // V2 returns no parent name — the listed folder's own displayName is the
                    // same value V1's getLeanItems envelope carried.
                    onFolderNameChanged.value = record.displayName
                    stampParentArchiveNr(records)
                    existsRecords.value = records.isNotEmpty()
                    pager.commit(records)
                    onRecordsRetrieved.value = records.toMutableList()
                }

                override fun onFailed(error: String?) {
                    endListing()
                    if (BuildConfig.DEBUG) Log.d(
                        TAG, "V2 children of folder $folderId failed ($error), falling back to V1"
                    )
                    loadFilesOfV1(archiveNr, folderLinkId)
                }
            })
    }

    // A paged listing shows skeleton rows instead of the spinner.
    private fun startListing(archiveNr: String) {
        isListing = true
        listedArchiveNr = archiveNr
        if (usesPagedList) {
            pager.startExternalListing(showsSkeleton = true)
            existsRecords.value = true
            onRecordsRetrieved.value = mutableListOf()
        } else {
            isBusy.value = true
        }
    }

    private fun endListing() {
        isListing = false
        isBusy.value = false
    }

    private fun stampParentArchiveNr(records: List<Record>) {
        records.forEach { it.parentFolderArchiveNr = listedArchiveNr }
    }

    fun onListEndReached() = pager.loadNextPage()

    fun onRetryNextPageClick() = pager.retryNextPage()

    private fun getRecords(
        recordVOs: List<RecordVO>, parentFolderArchiveNr: String
    ): MutableList<Record> {
        val records = ArrayList<Record>()
        for (recordVO in recordVOs) {
            val record = Record(recordVO)
            record.parentFolderArchiveNr = parentFolderArchiveNr
            records.add(record)
        }
        return records
    }

    /**
     * @return true if Up navigation completed successfully, false otherwise.
     */
    fun onNavigateUp(): Boolean {
        if (folderPathStack.isEmpty()) return false
        // Popping the record of the current folder
        folderPathStack.pop()
        if (folderPathStack.isEmpty()) {
            return false
        } else {
            val previousFolder = folderPathStack.peek()
            loadFilesOf(previousFolder)
        }
        return true
    }

    fun getCurrentFolder(): Record? = folderPathStack.lastOrNull()

    fun getOnFolderNameChanged(): MutableLiveData<String> = onFolderNameChanged

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getShowMessage(): LiveData<String> = showMessage

    fun getOnRecordsRetrieved(): LiveData<MutableList<Record>> = onRecordsRetrieved

    fun getOnRecordsAppended(): LiveData<List<Record>> = pager.appended

    fun getListFooter(): LiveData<ListFooter> = pager.footer

    fun getOnFileViewRequest(): MutableLiveData<ArrayList<Record>> = onFileViewRequest
}
