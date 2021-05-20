package org.permanent.permanent.models

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import org.permanent.permanent.ui.myFiles.OnFinishedListener
import org.permanent.permanent.ui.myFiles.download.*
import org.permanent.permanent.ui.myFiles.upload.WORKER_INPUT_FOLDER_LINK_ID_KEY
import java.util.*

const val FILE_DOWNLOAD_TAG = "file_download_tag"
class Download private constructor(val context: Context, val listener: OnFinishedListener) {
    private lateinit var workInfoLiveData: LiveData<WorkInfo>
    private lateinit var uuid: UUID
    private lateinit var displayName: String
    private var workRequest: OneTimeWorkRequest? = null
    val isEnqueued = MutableLiveData(false)
    val isDownloading = MutableLiveData(false)
    val progress = MutableLiveData(0)

    constructor(
        context: Context,
        record: Record,
        listener: OnFinishedListener
    ) : this(context, listener) {
        displayName = record.displayName ?: ""
        val folderLinkId = record.folderLinkId
        val recordId = record.recordId
        if (folderLinkId != null && recordId != null) {
            val builder = Data.Builder().apply {
                putInt(WORKER_INPUT_FOLDER_LINK_ID_KEY, folderLinkId)
                putInt(WORKER_INPUT_RECORD_ID_KEY, recordId)
            }
            workRequest = OneTimeWorkRequest.Builder(DownloadWorker::class.java)
                .addTag(displayName) // sync with the other secondary constructor
                .addTag(FILE_DOWNLOAD_TAG)
                .setInputData(builder.build())
                .build()
            uuid = workRequest!!.id
            workInfoLiveData = WorkManager.getInstance(context).getWorkInfoByIdLiveData(uuid)
        }
    }

    constructor(context: Context, workInfo: WorkInfo, listener: OnFinishedListener
    ) : this(context, listener) {
        for (tag in workInfo.tags) {
            if (!tag.contains(DownloadWorker::class.java.simpleName)
                && !tag.contains(FILE_DOWNLOAD_TAG))
                displayName = tag
        }
        uuid = workInfo.id
        workInfoLiveData = WorkManager.getInstance(context).getWorkInfoByIdLiveData(uuid)
    }

    fun getDisplayName() = displayName

    fun getWorkRequest() = workRequest

    fun observeWorkInfoOn(lifecycleOwner: LifecycleOwner) {
        workInfoLiveData.observe(lifecycleOwner, workInfoObserver)
    }

    private fun removeWorkInfoObserver() {
        workInfoLiveData.removeObserver(workInfoObserver)
    }

    private val workInfoObserver = Observer<WorkInfo> { workInfo ->
        val progressValue = workInfo.progress.getInt(DOWNLOAD_PROGRESS, 0)
        val state = workInfo.state
        progress.value = progressValue
        val isEnqueuedValue = state == WorkInfo.State.ENQUEUED || state == WorkInfo.State.RUNNING && progressValue == 0
        if(isEnqueued.value != isEnqueuedValue) isEnqueued.value = isEnqueuedValue
        val isDownloadingValue = state == WorkInfo.State.RUNNING && progressValue != 0
        if(isDownloading.value != isDownloadingValue) isDownloading.value = isDownloadingValue
        if (state.isFinished) {
            removeWorkInfoObserver()
            listener.onFinished(this)
        }
    }
}