package org.permanent.permanent.ui.myFiles

import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver
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

    // Lists sit in different scroll containers, so any scroll in the window is checked.
    private val onWindowScroll = ViewTreeObserver.OnScrollChangedListener { checkEndReached() }

    init {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) = checkEndReached()
        })
        recyclerView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(view: View) =
                view.viewTreeObserver.addOnScrollChangedListener(onWindowScroll)

            override fun onViewDetachedFromWindow(view: View) =
                view.viewTreeObserver.removeOnScrollChangedListener(onWindowScroll)
        })
        if (recyclerView.isAttachedToWindow) {
            recyclerView.viewTreeObserver.addOnScrollChangedListener(onWindowScroll)
        }
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
        if (skeleton.getGlobalVisibleRect(visibleRect)) onEndReached()
    }

    companion object {
        private const val GRID_SPAN_COUNT = 2
    }
}
