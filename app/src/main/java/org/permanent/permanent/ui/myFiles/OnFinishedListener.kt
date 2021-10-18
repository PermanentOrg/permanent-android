package org.permanent.permanent.ui.myFiles

import androidx.work.WorkInfo
import org.permanent.permanent.models.Download
import org.permanent.permanent.models.Upload

interface OnFinishedListener {
    fun onFinished(upload: Upload, succeeded: Boolean)
    fun onFinished(download: Download, state: WorkInfo.State)
    fun onFailedUpload(message: String)
    fun onQuotaExceeded()
}