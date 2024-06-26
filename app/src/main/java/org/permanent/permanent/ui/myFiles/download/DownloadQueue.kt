package org.permanent.permanent.ui.myFiles.download

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkManager
import org.permanent.permanent.models.Download
import org.permanent.permanent.models.FILE_DOWNLOAD_TAG
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.myFiles.OnFinishedListener

class DownloadQueue(
    val context: Context,
    val lifecycleOwner: LifecycleOwner,
    private val onFinishedListener: OnFinishedListener
) {
    private val enqueuedDownloads: MutableLiveData<MutableList<Download>> = MutableLiveData()
    private val workManager = WorkManager.getInstance(context)

    init {
        enqueuedDownloads.value = ArrayList()
        val workInfoList = workManager.getWorkInfosByTag(FILE_DOWNLOAD_TAG).get()
        for (workInfo in workInfoList) {
            if (!workInfo.state.isFinished) {
                val restoredDownload = Download(context, workInfo, onFinishedListener)
                restoredDownload.observeWorkInfoOn(lifecycleOwner)
                enqueuedDownloads.value?.add(restoredDownload)
            }
        }
        notifyEnqueuedDownloadsChanged()
    }

    private fun notifyEnqueuedDownloadsChanged() {
        enqueuedDownloads.value = enqueuedDownloads.value
    }

    fun enqueueNewDownloadFor(record: Record): Download {
        val download = Download(context, record, onFinishedListener)
        download.getWorkRequest()?.let { workManager.enqueue(it) }
        download.observeWorkInfoOn(lifecycleOwner)
        enqueuedDownloads.value?.add(download)
        notifyEnqueuedDownloadsChanged()

        return download
    }

    fun removeDownload(download: Download) {
        val enqueuedDownloadsValue = enqueuedDownloads.value
        if (!enqueuedDownloadsValue.isNullOrEmpty()) {
            enqueuedDownloadsValue.remove(download)
        }
    }

    fun getEnqueuedDownloadsLiveData(): MutableLiveData<MutableList<Download>> {
        return enqueuedDownloads
    }
}