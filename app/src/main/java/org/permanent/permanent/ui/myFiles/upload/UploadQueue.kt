package org.permanent.permanent.ui.myFiles.upload

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkContinuation
import androidx.work.WorkManager
import org.permanent.permanent.models.FolderIdentifier
import org.permanent.permanent.models.Upload
import org.permanent.permanent.ui.myFiles.OnFinishedListener

class UploadQueue(
    val context: Context,
    private val folderIdentifier: FolderIdentifier,
    val lifecycleOwner: LifecycleOwner,
    val id: String,
    private val onFinishedListener: OnFinishedListener
) {
    private val pendingUploads: MutableList<Upload> = ArrayList()
    private val enqueuedUploads: MutableLiveData<MutableList<Upload>> = MutableLiveData()
    private var workContinuation: WorkContinuation? = null

    init {
        enqueuedUploads.value = ArrayList()
        val workInfoList = WorkManager.getInstance(context).getWorkInfosForUniqueWork(id).get()
        for (workInfo in workInfoList) {
            if (!workInfo.state.isFinished) {
                val restoredUpload = Upload(context, workInfo, onFinishedListener)
                restoredUpload.observeWorkInfoOn(lifecycleOwner)
                enqueuedUploads.value?.add(restoredUpload)
            }
        }
        notifyObserversOnEnqueuedUploadsChanged()
    }

    private fun notifyObserversOnEnqueuedUploadsChanged() {
        enqueuedUploads.value = enqueuedUploads.value
    }

    fun addNewUploadFor(uri: Uri) {
        add(Upload(context, folderIdentifier, uri, onFinishedListener))
    }

    @SuppressLint("EnqueueWork")
    fun add(upload: Upload): UploadQueue {
        workContinuation = if (workContinuation == null) {
            upload.getWorkRequest()?.let {
                WorkManager.getInstance(context)
                    .beginUniqueWork(id, ExistingWorkPolicy.APPEND_OR_REPLACE, it) }
        } else {
            upload.getWorkRequest()?.let { workContinuation?.then(it) }
        }
        pendingUploads.add(upload)
        return this
    }

    fun enqueuePendingUploads() {
        workContinuation?.enqueue()
        workContinuation = null
        for (upload in pendingUploads) {
            upload.observeWorkInfoOn(lifecycleOwner)
            enqueuedUploads.value?.add(upload)
        }
        notifyObserversOnEnqueuedUploadsChanged()
        pendingUploads.clear()
    }

    fun getEnqueuedUploadsLiveData(): MutableLiveData<MutableList<Upload>> {
        return enqueuedUploads
    }

    fun removeListeners() {
        if (!enqueuedUploads.value.isNullOrEmpty()) {
            for (upload in enqueuedUploads.value!!) {
                upload.removeWorkInfoObserver()
            }
            enqueuedUploads.value!!.clear()
        }
    }
}
