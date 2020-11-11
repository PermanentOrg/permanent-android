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
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.ui.myFiles.download.*
import org.permanent.permanent.ui.myFiles.upload.WORKER_INPUT_FOLDER_LINK_ID_KEY
import java.util.*

const val FILE_DOWNLOAD_TAG = "file_download_tag"
class Download private constructor(val context: Context, val listener: IOnFinishedListener) {
    private lateinit var workInfoLiveData: LiveData<WorkInfo>
    private lateinit var uuid: UUID
    private lateinit var displayName: String
    private var workRequest: OneTimeWorkRequest? = null
    val isDownloading = MutableLiveData(false)
    val progress = MutableLiveData(0)

    constructor(
        context: Context,
        file: RecordVO,
        listener: IOnFinishedListener
    ) : this(context, listener) {
        displayName = file.displayName ?: ""
        val folderLinkId = file.folder_linkId
        val archiveNr = file.archiveNbr
        val archiveId = file.archiveId
        val recordId = file.recordId
        if (folderLinkId != null && archiveNr != null && archiveId != null && recordId != null) {
            val builder = Data.Builder().apply {
                putInt(WORKER_INPUT_FOLDER_LINK_ID_KEY, folderLinkId)
                putString(WORKER_INPUT_ARCHIVE_NR_KEY, archiveNr)
                putInt(WORKER_INPUT_ARCHIVE_ID_KEY, archiveId)
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

    constructor(context: Context, workInfo: WorkInfo, listener: IOnFinishedListener
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

    fun removeWorkInfoObserver() {
        workInfoLiveData.removeObserver(workInfoObserver)
    }

    private val workInfoObserver = Observer<WorkInfo> { workInfo ->
        val state = workInfo.state
        if (state.isFinished) {
            listener.onFinished(this)
            return@Observer
        }
        if(isDownloading.value == false) isDownloading.value = state == WorkInfo.State.RUNNING
        val progressValue = workInfo.progress.getInt(DOWNLOAD_PROGRESS, 0)
        progress.value = progressValue
    }

    interface IOnFinishedListener {
        fun onFinished(download: Download)
    }
}