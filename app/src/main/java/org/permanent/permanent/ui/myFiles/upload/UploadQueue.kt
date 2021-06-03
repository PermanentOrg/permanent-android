package org.permanent.permanent.ui.myFiles.upload

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkContinuation
import androidx.work.WorkManager
import org.permanent.permanent.models.Account
import org.permanent.permanent.models.NavigationFolderIdentifier
import org.permanent.permanent.models.Upload
import org.permanent.permanent.repositories.AccountRepositoryImpl
import org.permanent.permanent.repositories.IAccountRepository
import org.permanent.permanent.ui.myFiles.OnFinishedListener

class UploadQueue(
    val context: Context,
    val lifecycleOwner: LifecycleOwner,
    private val queueId: String,
    private val folderIdentifier: NavigationFolderIdentifier,
    private val onFinishedListener: OnFinishedListener
) {
    private val pendingUploads: MutableList<Upload> = ArrayList()
    private val enqueuedUploads: MutableLiveData<MutableList<Upload>> = MutableLiveData()
    private var workContinuation: WorkContinuation? = null
    private var accountRepository: IAccountRepository? = AccountRepositoryImpl(context)

    init {
        enqueuedUploads.value = ArrayList()
        val workInfoList = WorkManager.getInstance(context).getWorkInfosForUniqueWork(queueId).get()
        for (workInfo in workInfoList) {
            if (!workInfo.state.isFinished) {
                val restoredUpload = Upload(context, workInfo, onFinishedListener)
                restoredUpload.observeWorkInfoOn(lifecycleOwner)
                enqueuedUploads.value?.add(restoredUpload)
            }
        }
        notifyEnqueuedUploadsChanged()
    }

    private fun notifyEnqueuedUploadsChanged() {
        enqueuedUploads.value = enqueuedUploads.value
    }

    fun getEnqueuedUploadsLiveData(): MutableLiveData<MutableList<Upload>> {
        return enqueuedUploads
    }

    fun upload(uris: List<Uri>) {
        val copiedUris: List<Uri> = ArrayList(uris)
        accountRepository?.getAccount(object : IAccountRepository.IAccountListener {
            override fun onSuccess(account: Account) {
                for (uri in copiedUris) {
                    if (context.assetSize(uri) < account.spaceLeft!!) {
                        val upload = Upload(context, folderIdentifier, uri, onFinishedListener)
                        pendingUploads.add(upload)
                    } else {
                        onFinishedListener.onFailed("Quota exceeded")
                        return
                    }
                }
                enqueuePendingUploads()
            }

            override fun onFailed(error: String?) {
                onFinishedListener.onFailed(error!!)
            }
        })
    }

    fun Context.assetSize(resourceUri: Uri): Long {
        try {
            val descriptor = contentResolver.openAssetFileDescriptor(resourceUri, "r")
            val size = descriptor?.length ?: return 0
            descriptor.close()
            return size
        } catch (e: Resources.NotFoundException) {
            return 0
        }
    }

    @SuppressLint("EnqueueWork")
    fun enqueuePendingUploads(
        existingWorkPolicy: ExistingWorkPolicy = ExistingWorkPolicy.APPEND_OR_REPLACE
    ) {
        if (pendingUploads.size != 0) {
            for (upload in pendingUploads) {
                workContinuation = if (workContinuation == null) {
                    upload.getWorkRequest()?.let {
                        WorkManager.getInstance(context)
                            .beginUniqueWork(queueId, existingWorkPolicy, it)
                    }
                } else {
                    upload.getWorkRequest()?.let { workContinuation?.then(it) }
                }
            }
            workContinuation?.enqueue()
            workContinuation = null
            observePendingUploads()
        }
    }

    private fun observePendingUploads() {
        for (upload in pendingUploads) {
            upload.observeWorkInfoOn(lifecycleOwner)
            enqueuedUploads.value?.add(upload)
        }
        pendingUploads.clear()
        notifyEnqueuedUploadsChanged()
    }

    fun clearEnqueuedUploadsAndRemoveTheirObservers() {
        if (!enqueuedUploads.value.isNullOrEmpty()) {
            for (upload in enqueuedUploads.value!!) {
                upload.removeWorkInfoObserver()
            }
            enqueuedUploads.value!!.clear()
        }
    }

    fun prepareToRequeueUploadsExcept(cancelledUpload: Upload) {
        val enqueuedUploadsValue = enqueuedUploads.value

        if (!enqueuedUploadsValue.isNullOrEmpty()) {
            for (upload in enqueuedUploadsValue) {
                if (upload != cancelledUpload) {
                    val newUpload =
                        Upload(context, folderIdentifier, upload.getUri(), onFinishedListener)
                    pendingUploads.add(newUpload)
                }
            }
        }
    }

    fun removeFinishedUpload(upload: Upload) {
        val enqueuedUploadsValue = enqueuedUploads.value
        if (!enqueuedUploadsValue.isNullOrEmpty()) {
            enqueuedUploadsValue.remove(upload)
        }
    }
}
