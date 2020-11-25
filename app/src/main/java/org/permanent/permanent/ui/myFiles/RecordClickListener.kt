package org.permanent.permanent.ui.myFiles

import org.permanent.permanent.models.Record

interface RecordClickListener {
    fun onRecordClick(record: Record)
}