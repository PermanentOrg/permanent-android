package org.permanent.permanent.models

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import org.permanent.permanent.ui.myFiles.upload.UPLOAD_PROGRESS
import org.permanent.permanent.ui.myFiles.upload.UploadWorker
import org.permanent.permanent.ui.myFiles.upload.WORKER_INPUT_URI_KEY
import org.permanent.permanent.ui.myFiles.upload.WORKER_TAG_UPLOAD
import java.io.IOException
import java.util.*

class Upload(val context: Context, uri: Uri, val listener: IOnFinishedListener) {
    private var uuid: UUID
    private var workRequest: OneTimeWorkRequest
    val displayName: String? = getName(uri)
    val isUploading = MutableLiveData(false)
    val progress = MutableLiveData(0)

    init {
        val builder = Data.Builder().apply { putString(WORKER_INPUT_URI_KEY, uri.toString()) }
        workRequest = OneTimeWorkRequest.Builder(UploadWorker::class.java)
            .addTag(WORKER_TAG_UPLOAD)
            .setInputData(builder.build())
            .build()
        uuid = workRequest.id
    }

    private fun getName(uri: Uri): String? {
        var displayName: String? = null
        val cursor = context.contentResolver.query(uri, null, null,
            null, null)
        try {
            val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor?.moveToFirst()
            displayName = nameIndex?.let { cursor.getString(it) }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return displayName
    }

    fun getWorkRequest() = workRequest

    fun observeWorkInfo(lifecycleOwner: LifecycleOwner) {
        WorkManager.getInstance(context).getWorkInfoByIdLiveData(uuid).observe(lifecycleOwner, {
            val state = it.state
            if (state.isFinished) {
                listener.onFinished(this)
                return@observe
            }
            isUploading.value = (state == WorkInfo.State.ENQUEUED || state == WorkInfo.State.RUNNING)
            progress.value = it.progress.getInt(UPLOAD_PROGRESS, 0)
        })
    }

    interface IOnFinishedListener {
        fun onFinished(upload: Upload)
    }
}