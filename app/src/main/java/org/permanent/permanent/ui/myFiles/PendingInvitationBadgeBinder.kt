package org.permanent.permanent.ui.myFiles

import android.view.View
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.formatPendingInvitationCount
import org.permanent.permanent.ui.pendingInvitationCount

class PendingInvitationBadgeBinder(
    private val badgeView: TextView,
    private val overflowButton: View,
) {
    private var observer: Observer<Boolean>? = null
    private var observedSelectMode: MutableLiveData<Boolean>? = null

    fun bind(record: Record, lifecycleOwner: LifecycleOwner, enabled: Boolean) {
        detach()
        val selectMode = record.isSelectMode
        if (selectMode == null) {
            apply(record, enabled)
            return
        }
        val newObserver = Observer<Boolean> { apply(record, enabled) }
        selectMode.observe(lifecycleOwner, newObserver)
        observer = newObserver
        observedSelectMode = selectMode
    }

    private fun detach() {
        val current = observer ?: return
        observedSelectMode?.removeObserver(current)
        observer = null
        observedSelectMode = null
    }

    private fun apply(record: Record, enabled: Boolean) {
        val count = record.pendingInvitationCount
        val show = enabled
                && overflowButton.visibility == View.VISIBLE
                && record.isSelectMode?.value != true
                && count > 0
        val targetVisibility = if (show) View.VISIBLE else View.GONE
        if (badgeView.visibility != targetVisibility) badgeView.visibility = targetVisibility
        if (show) {
            val text = formatPendingInvitationCount(count)
            if (badgeView.text?.toString() != text) badgeView.text = text
        }
    }
}
