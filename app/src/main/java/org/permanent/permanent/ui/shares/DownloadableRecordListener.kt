package org.permanent.permanent.ui.shares

import org.permanent.permanent.ui.myFiles.download.DownloadableRecord

interface DownloadableRecordListener {
    fun onRecordClick(record: DownloadableRecord)
    fun onRecordOptionsClick(record: DownloadableRecord)
    fun onCancelClick(record: DownloadableRecord)
}