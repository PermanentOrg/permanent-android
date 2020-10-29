package org.permanent.permanent.models

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkInfo
import java.io.IOException
import java.util.*


class Upload(context: Context, val uri: Uri) {
    lateinit var uploadId: UUID
    val displayName: String? = getName(context, uri)
    val isUploading = MutableLiveData(false)

    private fun getName(context: Context, uri: Uri): String? {
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

    fun setId(id: UUID) {
        uploadId = id
    }

    fun setState(state: WorkInfo.State) {
        Log.d("Upload", state.name + uploadId)
        isUploading.value = (state == WorkInfo.State.ENQUEUED || state == WorkInfo.State.RUNNING)
    }
}