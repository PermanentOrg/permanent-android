package org.permanent.permanent.ui.myFiles

import org.permanent.permanent.models.Download
import org.permanent.permanent.models.Upload

interface OnFinishedListener {
    fun onFinished(upload: Upload, succeeded: Boolean)
    fun onFinished(download: Download)
    fun onFailed(message: String)

}