package org.permanent.permanent.models

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import org.permanent.permanent.ui.getDisplayName
import org.permanent.permanent.ui.myFiles.OnFinishedListener
import org.permanent.permanent.ui.myFiles.upload.*
import java.util.*

class Upload private constructor(val context: Context, val listener: OnFinishedListener) {
    private lateinit var workInfoLiveData: LiveData<WorkInfo>
    private lateinit var uuid: UUID
    private lateinit var uri: Uri
    private lateinit var displayName: String
    private var workRequest: OneTimeWorkRequest? = null
    val isUploading = MutableLiveData(false)
    val progress = MutableLiveData(0)

    constructor(
        context: Context,
        folderIdentifier: NavigationFolderIdentifier,
        uri: Uri,
        listener: OnFinishedListener
    ) : this(context, listener) {
        this.uri = uri
        displayName = uri.getDisplayName(context)
        val builder = Data.Builder().apply {
            putInt(WORKER_INPUT_FOLDER_ID_KEY, folderIdentifier.folderId)
            putInt(WORKER_INPUT_FOLDER_LINK_ID_KEY, folderIdentifier.folderLinkId)
            putString(WORKER_INPUT_URI_KEY, uri.toString())
        }
        workRequest = OneTimeWorkRequest.Builder(UploadWorker::class.java)
            .addTag(displayName) // sync with the other secondary constructor
            .setInputData(builder.build())
            .build()
        uuid = workRequest!!.id
        workInfoLiveData = WorkManager.getInstance(context).getWorkInfoByIdLiveData(uuid)
    }

    constructor(
        context: Context, workInfo: WorkInfo, listener: OnFinishedListener
    ) : this(context, listener) {
        for (tag in workInfo.tags) {
            if (!tag.contains(UploadWorker::class.java.simpleName)) displayName = tag
        }
        uuid = workInfo.id
        workInfoLiveData = WorkManager.getInstance(context).getWorkInfoByIdLiveData(uuid)
    }

    fun getUri() = uri

    fun getDisplayName() = displayName

    fun getWorkRequest() = workRequest

    private val workInfoObserver = Observer<WorkInfo> { workInfo ->
        if (workInfo != null) {
            val progressValue = workInfo.progress.getInt(UPLOAD_PROGRESS, 0)
            progress.value = progressValue

            val state = workInfo.state
            val isUploadingValue = state == WorkInfo.State.RUNNING && progressValue != 0
            if (isUploading.value != isUploadingValue) isUploading.value = isUploadingValue

            if (state.isFinished) {
                listener.onFinished(this, state == WorkInfo.State.SUCCEEDED)
                removeWorkInfoObserver()
            }
        } else {
            listener.onFinished(this, false)
        }
    }

    fun observeWorkInfoOn(lifecycleOwner: LifecycleOwner) {
        workInfoLiveData.observe(lifecycleOwner, workInfoObserver)
    }

    fun removeWorkInfoObserver() {
        workInfoLiveData.removeObserver(workInfoObserver)
    }

    fun cancel() {
        WorkManager.getInstance(context).cancelWorkById(uuid)
    }
}