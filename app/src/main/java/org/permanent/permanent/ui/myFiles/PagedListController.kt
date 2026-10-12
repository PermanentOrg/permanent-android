package org.permanent.permanent.ui.myFiles

import android.graphics.Rect
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// Puts the list footer after a records adapter and asks for the next page once the
// next-page skeleton comes on screen.
class PagedListController(
    private val recyclerView: RecyclerView,
    private val onEndReached: () -> Unit,
    onRetry: () -> Unit
) {
    private val footerAdapter = ListFooterAdapter(onRetry)
    private var recordsAdapter: RecyclerView.Adapter<*>? = null
    private var footer: ListFooter = ListFooter.None
    private var isGrid = false
    private val visibleRect = Rect()
    private var isEndReachedPosted = false

    init {
        recyclerView.itemAnimator?.addDuration = ROWS_FADE_IN_MS
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) = checkEndReached()
        })
    }

    fun attach(recordsAdapter: RecyclerView.Adapter<*>, isGrid: Boolean) {
        this.recordsAdapter = recordsAdapter
        this.isGrid = isGrid
        footerAdapter.set(footer, isGrid)
        recyclerView.layoutManager = if (isGrid) {
            GridLayoutManager(recyclerView.context, GRID_SPAN_COUNT).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        val footerPosition = position - recordsAdapter.itemCount
                        return if (footerPosition >= 0 && footerAdapter.isFullSpan(footerPosition)) {
                            GRID_SPAN_COUNT
                        } else 1
                    }
                }
            }
        } else {
            LinearLayoutManager(recyclerView.context)
        }
        recyclerView.adapter = ConcatAdapter(recordsAdapter, footerAdapter)
    }

    fun setFooter(footer: ListFooter) {
        if (footer == this.footer) return
        this.footer = footer
        footerAdapter.set(footer, isGrid)
        // A short page may leave the skeleton on screen without any scrolling.
        recyclerView.post { checkEndReached() }
    }

    private fun checkEndReached() {
        // Only an idle next-page skeleton asks; a page already on its way does not.
        if ((footer as? ListFooter.NextPage)?.isLoading != false) return
        val firstFooterPosition = recordsAdapter?.itemCount ?: return
        val skeleton = recyclerView.layoutManager?.findViewByPosition(firstFooterPosition) ?: return
        if (!skeleton.getGlobalVisibleRect(visibleRect) || isEndReachedPosted) return
        // Asking from a scroll callback would change the adapter mid-layout.
        isEndReachedPosted = true
        recyclerView.post {
            isEndReachedPosted = false
            onEndReached()
        }
    }

    companion object {
        private const val GRID_SPAN_COUNT = 2
        private const val ROWS_FADE_IN_MS = 250L
    }
}
