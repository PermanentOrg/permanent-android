package org.permanent.permanent.ui.myFiles.upload

import org.permanent.permanent.models.Upload

interface UploadCancelListener {
    fun onCancelClick(upload: Upload)
}