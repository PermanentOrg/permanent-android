package org.permanent.permanent.ui.myFiles

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.R

// Rows after a paged list: skeletons while a page loads, the page error with retry,
// or the count of everything listed.
class ListFooterAdapter(private val onRetry: () -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var footer: ListFooter = ListFooter.None
    private var isGrid = false
    private var viewTypes: List<Int> = emptyList()

    // Range updates only: a full refresh here would rebind every row of the ConcatAdapter.
    fun set(footer: ListFooter, isGrid: Boolean) {
        if (footer == this.footer && isGrid == this.isGrid) return
        this.footer = footer
        this.isGrid = isGrid
        val oldCount = viewTypes.size
        viewTypes = buildViewTypes()
        val newCount = viewTypes.size
        notifyItemRangeChanged(0, minOf(oldCount, newCount))
        if (newCount > oldCount) notifyItemRangeInserted(oldCount, newCount - oldCount)
        if (newCount < oldCount) notifyItemRangeRemoved(newCount, oldCount - newCount)
    }

    fun isFullSpan(position: Int) = viewTypes.getOrNull(position).let {
        it == TYPE_ERROR || it == TYPE_END
    }

    private fun buildViewTypes(): List<Int> {
        val skeleton = if (isGrid) TYPE_SKELETON_TILE else TYPE_SKELETON_ROW
        return when (footer) {
            ListFooter.None -> emptyList()
            ListFooter.FirstPage -> List(if (isGrid) FIRST_PAGE_TILES else FIRST_PAGE_ROWS) { skeleton }
            is ListFooter.NextPage -> List(if (isGrid) NEXT_PAGE_TILES else NEXT_PAGE_ROWS) { skeleton }
            ListFooter.Error ->
                List(if (isGrid) NEXT_PAGE_TILES else NEXT_PAGE_ROWS) { skeleton } + TYPE_ERROR
            is ListFooter.End -> listOf(TYPE_END)
        }
    }

    override fun getItemCount() = viewTypes.size

    override fun getItemViewType(position: Int) = viewTypes[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout = when (viewType) {
            TYPE_SKELETON_ROW -> R.layout.item_skeleton_row
            TYPE_SKELETON_TILE -> R.layout.item_skeleton_tile
            TYPE_ERROR -> R.layout.item_list_footer_error
            else -> R.layout.item_list_footer_end
        }
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return when (viewType) {
            TYPE_SKELETON_ROW, TYPE_SKELETON_TILE -> SkeletonViewHolder(view)
            else -> object : RecyclerView.ViewHolder(view) {}
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val view = holder.itemView
        // Not holder.itemViewType: ConcatAdapter renumbers the view types.
        when (viewTypes[position]) {
            TYPE_SKELETON_ROW, TYPE_SKELETON_TILE -> {
                holder as SkeletonViewHolder
                // The pulse runs only while a page is on its way.
                val isLoading = footer == ListFooter.FirstPage ||
                        (footer as? ListFooter.NextPage)?.isLoading == true
                holder.setAnimated(isLoading)
                // One announcement for the whole skeleton block.
                if (position == 0 && footer != ListFooter.Error) {
                    view.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                    view.contentDescription = view.context.getString(
                        if (footer == ListFooter.FirstPage) R.string.list_loading_items
                        else R.string.list_loading_more_items
                    )
                } else {
                    view.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                    view.contentDescription = null
                }
            }
            TYPE_ERROR -> view.findViewById<View>(R.id.btnRetry).setOnClickListener { onRetry() }
            TYPE_END -> {
                val end = footer as? ListFooter.End ?: return
                val resources = view.resources
                view.findViewById<TextView>(R.id.tvItemCount).text = resources.getString(
                    R.string.list_item_count,
                    resources.getQuantityString(R.plurals.list_folder_count, end.folders, end.folders),
                    resources.getQuantityString(R.plurals.list_file_count, end.files, end.files)
                )
            }
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        (holder as? SkeletonViewHolder)?.resume()
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        (holder as? SkeletonViewHolder)?.pause()
    }

    private class SkeletonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var isAnimated = false
        private val pulse = ObjectAnimator.ofFloat(view, View.ALPHA, 1f, PULSE_MIN_ALPHA).apply {
            duration = PULSE_MILLIS
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
        }

        fun setAnimated(animated: Boolean) {
            isAnimated = animated
            if (animated) resume() else pause()
        }

        fun resume() {
            if (isAnimated && !pulse.isStarted) pulse.start()
        }

        fun pause() {
            pulse.cancel()
            itemView.alpha = 1f
        }
    }

    companion object {
        private const val TYPE_SKELETON_ROW = 1
        private const val TYPE_SKELETON_TILE = 2
        private const val TYPE_ERROR = 3
        private const val TYPE_END = 4
        private const val FIRST_PAGE_ROWS = 7
        private const val FIRST_PAGE_TILES = 6
        private const val NEXT_PAGE_ROWS = 3
        private const val NEXT_PAGE_TILES = 2
        private const val PULSE_MILLIS = 800L
        private const val PULSE_MIN_ALPHA = 0.5f
    }
}
