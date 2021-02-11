package org.permanent.permanent.ui.myFiles.upload

import android.content.Context
import android.net.Uri
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import org.permanent.permanent.Constants
import org.permanent.permanent.network.models.GetPresignedUrlResponse
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.getDisplayName
import org.permanent.permanent.ui.getFile
import retrofit2.Call

/**
 * Worker class responsible for handling record uploads
 *
 * Mandatory input param WORKER_INPUT_URI_KEY - Uri of record to upload
 */

const val WORKER_INPUT_FOLDER_ID_KEY = "worker_input_folder_id_key"
const val WORKER_INPUT_FOLDER_LINK_ID_KEY = "worker_input_folder_link_id_key"
const val WORKER_INPUT_URI_KEY = "worker_input_uri_key"
const val UPLOAD_PROGRESS = "upload_progress"

class UploadWorker(val context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    private var callGetPresignedUrl: Call<GetPresignedUrlResponse>? = null
    private var callUpload: Call<ResponseBody>? = null
    private val prefsHelper = PreferencesHelper(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))
    private var fileRepository: IFileRepository = FileRepositoryImpl(context)

    override fun onStopped() {
        callGetPresignedUrl?.cancel()
        callUpload?.cancel()
    }

    override fun doWork(): Result {
        val folderId = inputData.getInt(WORKER_INPUT_FOLDER_ID_KEY, 0)
        val folderLinkId = inputData.getInt(WORKER_INPUT_FOLDER_LINK_ID_KEY, 0)
        val url = inputData.getString(WORKER_INPUT_URI_KEY)

        return if (url != null) {
            val uri = Uri.parse(url)
            var mediaType = context.contentResolver.getType(uri)?.toMediaTypeOrNull()
            if (mediaType == null) mediaType = Constants.MEDIA_TYPE_OCTET_STREAM.toMediaType()
            val displayName = uri.getDisplayName(applicationContext)
            val file = uri.getFile(context, displayName)

            if (file != null) {
                // #1 CALL: getPresignedUrl
                callGetPresignedUrl = fileRepository.getPresignedUrlForUpload(
                    folderId, folderLinkId, file, displayName, mediaType
                )
                val getPresignedUrlResponse = callGetPresignedUrl?.execute()?.body()
                prefsHelper.saveCsrf(getPresignedUrlResponse?.csrf)
                val uploadDestination = getPresignedUrlResponse?.getDestination()
                if (getPresignedUrlResponse?.isSuccessful == false || uploadDestination == null) {
                    file.delete()
                    Result.failure()
                }
                // #2 CALL: uploadFile
                callUpload = fileRepository.uploadFile(file, mediaType, uploadDestination!!,
                    object : CountingRequestListener {
                        override fun onProgressUpdate(progress: Long) {
                            setProgressAsync(
                                Data.Builder().putInt(UPLOAD_PROGRESS, progress.toInt()).build())
                        }
                    })
                val uploadResponse = callUpload?.execute()
                if (uploadResponse?.isSuccessful == false) {
                    file.delete()
                    Result.failure()
                }
                val destinationUrl = uploadDestination.destinationUrl
                if (destinationUrl == null) {
                    file.delete()
                    Result.failure()
                }
                // #3 CALL: registerRecord
                val registerRecordResponse = fileRepository.registerRecord(
                    folderId, folderLinkId, file, displayName, destinationUrl!!
                ).execute().body()
                prefsHelper.saveCsrf(registerRecordResponse?.csrf)
                if (registerRecordResponse?.isSuccessful == false) {
                    file.delete()
                    Result.failure()
                }

                file.delete()
                Result.success()
            } else {
                Result.failure()
            }
        } else {
            Result.failure()
        }
    }
}
