package org.permanent.permanent.ui.myFiles.download

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import okhttp3.ResponseBody
import org.permanent.permanent.models.FileType
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.myFiles.upload.WORKER_INPUT_FOLDER_LINK_ID_KEY
import retrofit2.Call
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

const val WORKER_INPUT_RECORD_ID_KEY = "worker_input_record_id"
const val DOWNLOAD_PROGRESS = "download_progress"
class DownloadWorker(val context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    private var callDownload: Call<ResponseBody>? = null
    private var fileRepository: IFileRepository = FileRepositoryImpl(context)

    override fun onStopped() {
        callDownload?.cancel()
    }

    override fun doWork(): Result {
        val folderLinkId = inputData.getInt(WORKER_INPUT_FOLDER_LINK_ID_KEY, 0)
        val recordId = inputData.getInt(WORKER_INPUT_RECORD_ID_KEY, 0)

        val fileData = fileRepository.getRecord(folderLinkId, recordId
        ).execute().body()?.getFileData()

        val downloadURL = fileData?.downloadURL
        val fileName = fileData?.fileName

        downloadURL?.let { url ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = applicationContext.contentResolver

                val collection: Uri = when {
                    fileData.contentType?.contains(FileType.IMAGE.toString()) == true ->
                        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    fileData.contentType?.contains(FileType.VIDEO.toString()) == true ->
                        MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    else -> MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                }

                val newFileDetails = ContentValues().apply { when {
                    fileData.contentType?.contains(FileType.IMAGE.toString()) == true ->
                        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    fileData.contentType?.contains(FileType.VIDEO.toString()) == true ->
                        put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                    else -> put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                } }
                resolver.insert(collection, newFileDetails)?.let { fileUri ->
                    resolver.openOutputStream(fileUri).use {
                        it?.let { outputStream ->
                            startDownloading(url, outputStream)
                        } ?: return Result.failure()
                    }
                } ?: return Result.failure()
            } else {
                fileName?.let { startDownloading(url, getFileOutputStream(it))
                } ?: return Result.failure()
            }
        } ?: return Result.failure()
        return Result.success()
    }

    private fun startDownloading(downloadURL: String, outputStream: OutputStream) {
        callDownload = fileRepository.downloadFile(downloadURL)
        val downloadResponse = callDownload?.execute()
        val contentLength = downloadResponse?.body()?.contentLength()
        val inputStream = downloadResponse?.body()?.byteStream()
        if (inputStream != null) {
            try {
                val updateInterval = 7
                val data = ByteArray(4 * 1024) // or other buffer size
                var totalCount = 0L
                var count: Int
                var reportedProgress = 0L
                while (inputStream.read(data).also { count = it } != -1) {
                    totalCount += count
                    outputStream.write(data, 0, count)
                    // Report progress
                    if (contentLength != null && contentLength > 0) {
                        val newProgress = 100 * totalCount / contentLength
                        if (newProgress >= reportedProgress + updateInterval) {
                            reportedProgress = newProgress
                            setProgressAsync(Data.Builder()
                                .putInt(DOWNLOAD_PROGRESS, reportedProgress.toInt()).build())
                        }
                    }
                }
                outputStream.flush()
            } catch (e: Exception) {
                Log.e(FileRepositoryImpl::class.java.simpleName, e.message!!)
                return
            } finally {
                inputStream.close()
                outputStream.close()
            }
        }
    }

    private fun getFileOutputStream(fileName : String) : FileOutputStream {
        var file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName)

        if(file.exists()) {
            var i = 1
            val parts = fileName.split(".")
            val name = parts[0]
            val exts = parts[1]
            while (file.exists()) {
                file = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "$name ($i).$exts"
                )
                i++
            }
        }
        return FileOutputStream(file)
    }
}