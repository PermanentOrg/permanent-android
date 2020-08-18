package org.permanent.permanent.ui.onboarding

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper

fun RecyclerView.attachSnapHelperWithListener(
    snapHelper: SnapHelper,
    behavior: SnapOnScrollListener.Behavior = SnapOnScrollListener.Behavior.NOTIFY_ON_SCROLL_STATE_IDLE,
    onSnapPositionChangeListener: OnSnapPositionChangeListener) {

    snapHelper.attachToRecyclerView(this)
    val snapOnScrollListener = SnapOnScrollListener(snapHelper, behavior, onSnapPositionChangeListener)
    addOnScrollListener(snapOnScrollListener)
}