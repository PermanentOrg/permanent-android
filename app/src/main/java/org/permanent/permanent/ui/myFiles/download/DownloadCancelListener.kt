package org.permanent.permanent.ui.myFiles.download

import org.permanent.permanent.models.Download

interface DownloadCancelListener {
    fun onCancelClick(download: Download)
}