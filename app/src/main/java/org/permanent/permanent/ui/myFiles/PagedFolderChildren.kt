package org.permanent.permanent.ui.myFiles

import android.content.Context
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.PermanentApplication
import org.permanent.permanent.R
import org.permanent.permanent.models.Record
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.viewmodels.SingleLiveEvent

// Shown when a folder can't be listed, instead of the raw network or server text.
fun listingFailureMessage(context: Context): String = context.getString(
    if (PermanentApplication.instance.connectivityMonitor.isConnected) R.string.generic_error
    else R.string.no_internet_connection
)

// A folder listing on a paged screen: the pager, every row listed so far, and what the
// list shows after them. Touched on main only.
class PagedFolderChildren(
    fileRepository: IFileRepository,
    private val prepareRecords: (List<Record>) -> Unit = {},
    private val onPagesOutOfSync: () -> Unit
) {
    private val loadedRecords = mutableListOf<Record>()
    val records: List<Record> get() = loadedRecords
    val footer = MutableLiveData<ListFooter>(ListFooter.None)
    val appended = SingleLiveEvent<List<Record>>()

    private val pager = FolderChildrenPager(fileRepository, object : FolderChildrenPager.Callback {
        override fun onPagingStateChanged(state: FolderChildrenPager.State) {
            // A refresh keeps the current footer until its reply lands.
            if (state != FolderChildrenPager.State.REFRESHING) updateFooter()
        }

        override fun onPageAppended(records: List<Record>) {
            prepareRecords(records)
            loadedRecords.addAll(records)
            appended.value = records
            updateFooter()
        }

        override fun onPagesOutOfSync() = this@PagedFolderChildren.onPagesOutOfSync()
    })

    val isComplete get() = pager.isComplete

    // Server order is the folder's saved sort; any other sort lists the whole folder.
    // A refresh keeps the rows already listed and shows no skeleton.
    fun list(
        folderId: Int,
        wholeFolder: Boolean,
        isRefresh: Boolean,
        listener: FolderChildrenPager.ListingListener
    ) {
        val pageSize = when {
            wholeFolder -> FolderChildrenPager.WHOLE_FOLDER
            isRefresh -> pager.refreshPageSize()
            else -> FolderChildrenPager.PAGE_SIZE
        }
        if (!isRefresh) loadedRecords.clear()
        pager.list(folderId, pageSize, showsSkeleton = !isRefresh, listener)
    }

    fun startExternalListing(showsSkeleton: Boolean) {
        if (showsSkeleton) loadedRecords.clear()
        pager.startExternalListing(showsSkeleton)
    }

    fun commit(records: List<Record>) {
        loadedRecords.clear()
        loadedRecords.addAll(records)
        updateFooter()
    }

    // The whole folder came by another route (the V1 failsafe).
    fun commitWholeFolder(records: List<Record>) {
        pager.markComplete(records)
        commit(records)
    }

    // Every route failed: a refresh keeps its rows, a first listing clears its skeleton.
    fun onListingFailed() {
        if (!pager.restoreAfterFailedRefresh()) reset()
    }

    fun reset() {
        pager.reset()
        loadedRecords.clear()
        updateFooter()
    }

    fun loadNextPage() = pager.loadNextPage()

    fun retryNextPage() = pager.retryNextPage()

    private fun updateFooter() {
        val next = ListFooter.of(pager.state, loadedRecords)
        if (footer.value != next) footer.value = next
    }
}
