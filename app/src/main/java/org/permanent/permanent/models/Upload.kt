package org.permanent.permanent.models

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import org.permanent.permanent.ui.myFiles.upload.*
import java.io.IOException
import java.util.*

class Upload private constructor(val context: Context, val listener: IOnFinishedListener) {
    private lateinit var workInfoLiveData: LiveData<WorkInfo>
    private lateinit var uuid: UUID
    private lateinit var displayName: String
    private var workRequest: OneTimeWorkRequest? = null
    val isUploading = MutableLiveData(false)
    val progress = MutableLiveData(0)

    constructor(
        context: Context,
        folderIdentifier: FolderIdentifier,
        uri: Uri,
        listener: IOnFinishedListener
    ) : this(context, listener) {
        displayName = getName(uri)
        val builder = Data.Builder().apply {
            putInt(WORKER_INPUT_FOLDER_ID_KEY, folderIdentifier.folderId)
            putInt(WORKER_INPUT_FOLDER_LINK_ID_KEY, folderIdentifier.folderLinkId)
            putString(WORKER_INPUT_URI_KEY, uri.toString())
            putString(WORKER_INPUT_FILE_DISPLAY_NAME_KEY, displayName)
        }
        val workReq = OneTimeWorkRequest.Builder(UploadWorker::class.java)
            .addTag(displayName) // sync with the other secondary constructor
            .setInputData(builder.build())
            .build()
        workRequest = workReq
        uuid = workReq.id
        workInfoLiveData = WorkManager.getInstance(context).getWorkInfoByIdLiveData(uuid)
    }

    constructor(context: Context, workInfo: WorkInfo, listener: IOnFinishedListener
    ) : this(context, listener) {
        for (tag in workInfo.tags) {
            if (!tag.contains(UploadWorker::class.java.simpleName)) displayName = tag
        }
        uuid = workInfo.id
        workInfoLiveData = WorkManager.getInstance(context).getWorkInfoByIdLiveData(uuid)
    }

    private fun getName(uri: Uri): String {
        var displayName = ""
        val cursor = context.contentResolver.query(uri, null, null,
            null, null)
        try {
            val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor?.moveToFirst()
            nameIndex?.let {  displayName = cursor.getString(it) }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return displayName
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
        isUploading.value = state == WorkInfo.State.RUNNING
        progress.value = workInfo.progress.getInt(UPLOAD_PROGRESS, 0)
    }

    interface IOnFinishedListener {
        fun onFinished(upload: Upload)
    }
}