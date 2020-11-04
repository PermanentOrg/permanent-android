package org.permanent.permanent.ui.myFiles.upload

import org.permanent.permanent.models.Upload

interface UploadCancelClickListener {
    fun onCancelClick(upload: Upload)
}