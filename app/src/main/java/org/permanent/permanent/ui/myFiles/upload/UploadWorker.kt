package org.permanent.permanent.ui.myFiles.upload

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import retrofit2.Call
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * Worker class responsible for handling record uploads
 *
 * Mandatory input param WORKER_INPUT_URI_KEY - Uri of record to upload
 */

const val WORKER_INPUT_FOLDER_ID_KEY = "worker_input_folder_id_key"
const val WORKER_INPUT_FOLDER_LINK_ID_KEY = "worker_input_folder_link_id_key"
const val WORKER_INPUT_URI_KEY = "worker_input_uri_key"
const val WORKER_INPUT_FILE_DISPLAY_NAME_KEY = "worker_input_file_name_key"
const val STATUS_OK = "OK"
const val UPLOAD_PROGRESS = "upload_progress"

class UploadWorker(val context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    private var call: Call<ResponseBody>? = null
    private var fileRepository: IFileRepository = FileRepositoryImpl(context)

    override fun onStopped() {
        call?.cancel()
    }

    override fun doWork(): Result {
        val folderId = inputData.getInt(WORKER_INPUT_FOLDER_ID_KEY, 0)
        val folderLinkId = inputData.getInt(WORKER_INPUT_FOLDER_LINK_ID_KEY, 0)
        val url = inputData.getString(WORKER_INPUT_URI_KEY)
        val displayName = inputData.getString(WORKER_INPUT_FILE_DISPLAY_NAME_KEY)

        return if (url != null) {
            var result = ""
            val uri = Uri.parse(url)
            val mediaType = context.contentResolver.getType(uri)?.toMediaTypeOrNull()
            val file = getFileToUpload(uri, displayName)

            if (mediaType != null && file != null) {
                call = fileRepository.startUploading(folderId, folderLinkId, file, displayName,
                    mediaType, object : CountingRequestListener {
                        override fun onProgressUpdate(progress: Long) {
                            setProgressAsync(
                                Data.Builder().putInt(UPLOAD_PROGRESS, progress.toInt()).build())
                        }
                    }, object : IResponseListener {
                        override fun onSuccess(message: String?) {
                        }

                        override fun onFailed(error: String?) {
                            error?.let { result = it }
                        }
                    })
                val responseBody = call?.execute()?.body()
                responseBody?.string()?.let { result = it }
                file.delete()

                if (result == STATUS_OK) {
                    Result.success()
                } else {
                    Log.d(UploadWorker::class.java.simpleName, result)
                    Result.failure()
                }
            } else {
                Log.d(UploadWorker::class.java.simpleName, "file or media type is null")
                Result.failure()
            }
        } else {
            Log.d(UploadWorker::class.java.simpleName, "url null")
            Result.failure()
        }
    }

    private fun getFileToUpload(uri: Uri, displayName: String?): File? {
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
                return file
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            output?.close()
        }
        return null
    }
}
