package org.permanent.permanent.ui.myFiles

import android.view.View
import androidx.core.view.isVisible
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.recordMenu.RecordMenuAnchor

interface RecordListener {
    fun onRecordClick(record: Record)
    fun onRecordOptionsClick(record: Record)
    fun onRecordCheckBoxClick(record: Record)
    fun onRecordDeleteClick(record: Record)
    fun onRecordLongClick(record: Record, anchor: RecordMenuAnchor): Boolean = false
}

fun RecordListener.attachLongClick(target: View, item: View, optionsButton: View, record: Record) {
    target.setOnLongClickListener {
        optionsButton.isVisible && onRecordLongClick(record, RecordMenuAnchor.of(item))
    }
}
