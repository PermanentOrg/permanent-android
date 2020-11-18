package org.permanent.permanent.ui.myFiles

import org.permanent.permanent.models.Download
import org.permanent.permanent.models.Upload

interface CancelListener {
    fun onCancelClick(upload: Upload)
    fun onCancelClick(download: Download)
}