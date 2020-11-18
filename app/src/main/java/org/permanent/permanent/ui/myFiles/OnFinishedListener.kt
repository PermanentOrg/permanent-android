package org.permanent.permanent.ui.myFiles

import org.permanent.permanent.models.Download
import org.permanent.permanent.models.Upload

interface OnFinishedListener {
    fun onFinished(upload: Upload)
    fun onFinished(download: Download)
}