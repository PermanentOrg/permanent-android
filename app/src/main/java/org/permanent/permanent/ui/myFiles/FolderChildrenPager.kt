package org.permanent.permanent.ui.myFiles

import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.network.StelaAccountService
import org.permanent.permanent.network.models.IFolderChildrenPageListener
import org.permanent.permanent.repositories.IFileRepository

// Lists a folder's V2 children page by page. Only the newest listing may commit, and a
// next page in flight is dropped as soon as another listing starts. Touched on main only.
class FolderChildrenPager(
    private val fileRepository: IFileRepository,
    private val callback: Callback
) {
    enum class State { NONE, LOADING_FIRST, REFRESHING, HAS_MORE, LOADING_MORE, FAILED, COMPLETE }

    interface Callback {
        fun onPagingStateChanged(state: State)
        fun onPageAppended(records: List<Record>)

        // A next page repeated rows already listed: the caller relists the whole folder.
        fun onPagesOutOfSync()
    }

    interface ListingListener {
        fun onSuccess(records: List<Record>)
        fun onFailed(error: String?)
        // Only a newer listing supersedes this one, and it owns the screen's state.
        fun onSuperseded() {}
    }

    var state = State.NONE
        private set(value) {
            field = value
            callback.onPagingStateChanged(value)
        }

    private var listingGeneration = 0
    private var nextPageGeneration = 0
    private var folderId = 0
    private var cursor: String? = null
    private val listedFolderLinkIds = HashSet<Int>()

    val isComplete get() = state == State.COMPLETE

    // A refresh keeps what the user has already scrolled through.
    fun refreshPageSize(): Int = (listedFolderLinkIds.size / PAGE_SIZE + 1) * PAGE_SIZE

    fun list(
        folderId: Int,
        pageSize: Int,
        showsSkeleton: Boolean,
        listener: ListingListener
    ) {
        val generation = ++listingGeneration
        nextPageGeneration++
        state = if (showsSkeleton) State.LOADING_FIRST else State.REFRESHING
        fileRepository.getChildrenPageV2(folderId, pageSize, null,
            object : IFolderChildrenPageListener {
                override fun onSuccess(records: List<Record>, nextCursor: String?) {
                    if (generation != listingGeneration) {
                        listener.onSuperseded()
                        return
                    }
                    this@FolderChildrenPager.folderId = folderId
                    listedFolderLinkIds.clear()
                    val unique = records.filter { listedFolderLinkIds.add(it.folderLinkId ?: -1) }
                    cursor = nextCursor
                    state = if (isLastPage(records.size, pageSize, nextCursor)) {
                        State.COMPLETE
                    } else {
                        State.HAS_MORE
                    }
                    listener.onSuccess(unique)
                }

                override fun onFailed(error: String?) {
                    if (generation != listingGeneration) {
                        listener.onSuperseded()
                        return
                    }
                    listener.onFailed(error)
                }
            })
    }

    fun loadNextPage() {
        if (state == State.HAS_MORE) fetchNextPage()
    }

    fun retryNextPage() {
        if (state == State.FAILED) fetchNextPage()
    }

    private fun fetchNextPage() {
        val generation = ++nextPageGeneration
        val requestedCursor = cursor
        state = State.LOADING_MORE
        fileRepository.getChildrenPageV2(folderId, PAGE_SIZE, requestedCursor,
            object : IFolderChildrenPageListener {
                override fun onSuccess(records: List<Record>, nextCursor: String?) {
                    if (generation != nextPageGeneration) return
                    val unique = records.filter { listedFolderLinkIds.add(it.folderLinkId ?: -1) }
                    if (records.isNotEmpty() && unique.isEmpty() && records.size == PAGE_SIZE) {
                        callback.onPagesOutOfSync()
                        return
                    }
                    cursor = nextCursor
                    state = if (isLastPage(records.size, PAGE_SIZE, nextCursor)) {
                        State.COMPLETE
                    } else {
                        State.HAS_MORE
                    }
                    if (unique.isNotEmpty()) callback.onPageAppended(unique)
                }

                override fun onFailed(error: String?) {
                    if (generation != nextPageGeneration) return
                    state = State.FAILED
                }
            })
    }

    // A listing by another route (V1, a sort save) starts: drop every reply in flight.
    fun startExternalListing(showsSkeleton: Boolean) {
        listingGeneration++
        nextPageGeneration++
        state = if (showsSkeleton) State.LOADING_FIRST else State.REFRESHING
    }

    // The folder was listed whole by another route (the V1 failsafe).
    fun markComplete(records: List<Record>) {
        nextPageGeneration++
        listedFolderLinkIds.clear()
        records.forEach { listedFolderLinkIds.add(it.folderLinkId ?: -1) }
        state = State.COMPLETE
    }

    // Drops every reply in flight, e.g. when the screen leaves paged listing.
    fun reset() {
        listingGeneration++
        nextPageGeneration++
        cursor = null
        listedFolderLinkIds.clear()
        state = State.NONE
    }

    // nextCursor is not null on the last page, so a short page also ends the list.
    private fun isLastPage(itemCount: Int, pageSize: Int, nextCursor: String?) =
        nextCursor == null || itemCount < pageSize

    companion object {
        const val PAGE_SIZE = StelaAccountService.CHILDREN_PAGE_SIZE
        const val WHOLE_FOLDER = StelaAccountService.MAX_CHILDREN_PAGE_SIZE
    }
}

// What the list shows after its rows.
sealed class ListFooter {
    object None : ListFooter()
    object FirstPage : ListFooter()
    data class NextPage(val isLoading: Boolean) : ListFooter()
    object Error : ListFooter()
    data class End(val folders: Int, val files: Int) : ListFooter()

    companion object {
        fun of(state: FolderChildrenPager.State, records: List<Record>): ListFooter = when (state) {
            FolderChildrenPager.State.LOADING_FIRST -> FirstPage
            FolderChildrenPager.State.HAS_MORE -> NextPage(isLoading = false)
            FolderChildrenPager.State.LOADING_MORE -> NextPage(isLoading = true)
            FolderChildrenPager.State.FAILED -> Error
            FolderChildrenPager.State.COMPLETE -> if (records.isEmpty()) None else End(
                folders = records.count { it.type == RecordType.FOLDER },
                files = records.count { it.type != RecordType.FOLDER }
            )
            FolderChildrenPager.State.NONE,
            FolderChildrenPager.State.REFRESHING -> None
        }
    }
}
