package org.permanent.permanent.ui.myFiles.upload

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.work.WorkManager
import org.permanent.permanent.models.Upload

class UploadQueue(context: Context, upload: Upload) {
    private val uploads: MutableList<Upload> = mutableListOf(upload)
    var workContinuation =
        WorkManager.getInstance(context).beginWith(upload.getWorkRequest())

    @SuppressLint("EnqueueWork")
    fun add(upload: Upload): UploadQueue {
        uploads.add(upload)
        workContinuation = workContinuation.then(upload.getWorkRequest())
        return this
    }

    fun enqueueOn(lifecycleOwner: LifecycleOwner): MutableList<Upload> {
        workContinuation.enqueue()
        for (upload in uploads) {
            upload.observeWorkInfo(lifecycleOwner)
        }
        return uploads
    }
}