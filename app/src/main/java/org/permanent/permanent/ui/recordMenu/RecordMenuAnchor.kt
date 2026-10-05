package org.permanent.permanent.ui.recordMenu

import android.graphics.Rect
import android.os.Parcelable
import android.view.View
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import kotlinx.parcelize.Parcelize
import org.permanent.permanent.ui.boundsOnScreen

@Parcelize
data class RecordMenuAnchor(
    val bounds: Rect,
    val washOutBounds: List<Rect>,
    val roundWashOutBounds: List<Rect> = emptyList()
) : Parcelable {

    fun withRoundWashOut(vararg views: View) =
        copy(roundWashOutBounds = views.filter { it.isShown }.map { it.boundsOnScreen() })

    companion object {
        fun of(item: View): RecordMenuAnchor {
            val bounds = item.boundsOnScreen()
            val list = item.parent as? RecyclerView
            val listBounds = list?.visibleBoundsOnScreen()
            if (list == null || listBounds == null) return RecordMenuAnchor(bounds, emptyList())
            // intersect() trims each row to the part of the list that is on screen
            val otherItems = list.children.filter { it !== item }
                .map { it.boundsOnScreen() }
                .filter { it.intersect(listBounds) }
                .toList()
            return RecordMenuAnchor(bounds, otherItems)
        }

        // The list can sit inside a scroll view, so its own bounds include rows scrolled out of sight
        private fun View.visibleBoundsOnScreen(): Rect? {
            val visible = Rect()
            if (!getGlobalVisibleRect(visible)) return null
            val onScreen = IntArray(2).also { getLocationOnScreen(it) }
            val inWindow = IntArray(2).also { getLocationInWindow(it) }
            visible.offset(onScreen[0] - inWindow[0], onScreen[1] - inWindow[1])
            return visible
        }
    }
}
