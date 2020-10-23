package org.permanent.permanent.ui.myFiles

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * Worker class responsible for handling record uploads
 *
 * Mandatory input param WORKER_INPUT_URI_KEY - Uri of record to upload
 */

const val WORKER_INPUT_URI_KEY = "WORKER_INPUT_URI_KEY"
const val WORKER_TAG_UPLOAD = "WORKER_TAG_UPLOAD"
const val UPLOAD_WORKER = "UPLOAD_WORKER"
private const val STATUS_OUT_OF_SPACE = "warning.financial.account.no_space_left"
private const val STATUS_ERROR = "Error"
const val STATUS_OK = "OK"

class UploadWorker(val context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    private var fileRepository: IFileRepository = FileRepositoryImpl(context)

    override fun doWork(): Result {
        val url = inputData.getString(WORKER_INPUT_URI_KEY)

        return if (url != null) {
            val uri = Uri.parse(url)
            val result = uploadFile(uri)

            if (result == STATUS_OK) {
                Log.d(UPLOAD_WORKER,"STATUS_OK")
                Result.success()
            } else {
                if (result == STATUS_OUT_OF_SPACE) {
                    Log.d(UPLOAD_WORKER,"no_space_left")
                } else {
                    Log.d(UPLOAD_WORKER,"visit_website")
                }
                Result.failure()
            }
        } else {
            Log.d(UPLOAD_WORKER,"url null")
            Result.failure()
        }
    }

    private fun uploadFile(uri: Uri): String? {
        val type = context.contentResolver.getType(uri)?.toMediaTypeOrNull()
        val returnCursor =
            applicationContext.contentResolver.query(uri, null, null,
                null, null)
        val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor?.moveToFirst()
        val displayName = nameIndex?.let { returnCursor.getString(it) }
        var output: OutputStream? = null
        try {
            val inputStream = applicationContext.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val file = File(applicationContext.cacheDir, displayName)
                output = FileOutputStream(file)

                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()

                return type?.let { fileRepository.startUploading(file, displayName, it) }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            output?.close()
        }

        return STATUS_ERROR
    }
}
