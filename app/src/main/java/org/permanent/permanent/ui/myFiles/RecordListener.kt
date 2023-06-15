package org.permanent.permanent.ui.myFiles

import org.permanent.permanent.models.Record

interface RecordListener {
    fun onRecordClick(record: Record)
    fun onRecordOptionsClick(record: Record)
    fun onRecordCheckBoxClick(record: Record)
    fun onRecordDeleteClick(record: Record)
}