package org.permanent.permanent.repositories

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.util.Log
import okhttp3.MediaType
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.models.FolderIdentifier
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.myFiles.RelocationType
import org.permanent.permanent.ui.myFiles.upload.CountingRequestListener
import org.permanent.permanent.ui.myFiles.upload.STATUS_OK
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class FileRepositoryImpl(val context: Context): IFileRepository {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val prefsHelper = PreferencesHelper(sharedPreferences)
    private val networkClient: NetworkClient = NetworkClient(context)

    override fun getMyFilesRecord(listener: IFileRepository.IOnMyFilesArchiveNrListener) {
        networkClient.getRoot(prefsHelper.getCsrf()).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()
                prefsHelper.saveCsrf(responseVO?.csrf)
                val myFilesRecord = responseVO?.getMyFilesRecord()

                if (myFilesRecord != null) {
                    listener.onSuccess(myFilesRecord)
                } else {
                    listener.onFailed(
                        responseVO?.Results?.get(0)?.message?.get(0)
                            ?: response.errorBody()?.toString()
                    )
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun getChildRecordsOf(
        myFilesArchiveNr: String,
        sort: String?,
        listener: IFileRepository.IOnRecordsRetrievedListener
    ) {
        navigateMin(myFilesArchiveNr, sort, listener)
    }

    override fun navigateMin(
        archiveNumber: String,
        sort: String?,
        listener: IFileRepository.IOnRecordsRetrievedListener
    ) {
        networkClient.navigateMin(prefsHelper.getCsrf(), archiveNumber)
            .enqueue(object : Callback<ResponseVO> {
                override fun onResponse(
                    call: Call<ResponseVO>,
                    response: Response<ResponseVO>
                ) {
                    val responseVO = response.body()
                    prefsHelper.saveCsrf(responseVO?.csrf)
                    val folderLinkIds: MutableList<Int> = ArrayList()
                    val childItemVOs: List<RecordVO?>? = response.body()?.getChildItemVOs()

                    if (childItemVOs != null) {
                        for (recordVO in childItemVOs) {
                            recordVO?.folder_linkId?.let { folderLinkIds.add(it) }
                        }
                        getLeanItems(archiveNumber, sort, folderLinkIds, listener)
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun getLeanItems(archiveNumber: String, sort: String?, childLinkIds: List<Int>,
                              listener: IFileRepository.IOnRecordsRetrievedListener
    ) {
        networkClient.getLeanItems(prefsHelper.getCsrf(), archiveNumber, sort, childLinkIds)
            .enqueue(object : Callback<ResponseVO> {

                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    prefsHelper.saveCsrf(responseVO?.csrf)
                    listener.onSuccess(responseVO?.getRecords())
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun createFolder(
        parentFolderIdentifier: FolderIdentifier,
        name: String,
        listener: IFileRepository.IOnResponseListener
    ) {
        networkClient.createFolder(
            prefsHelper.getCsrf(), name, parentFolderIdentifier.folderId,
            parentFolderIdentifier.folderLinkId
        ).enqueue(object : Callback<ResponseVO> {

            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()
                prefsHelper.saveCsrf(responseVO?.csrf)
                val firstMessage = responseVO?.getMessages()?.get(0)

                if (firstMessage != null && firstMessage.startsWith(Constants.FOLDER_CREATED_PREFIX))
                    listener.onSuccess(null)
                else listener.onFailed(context.getString(R.string.generic_error))
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun startUploading(
        folderId: Int, folderLinkId: Int, file: File, displayName: String?,
        mediaType: MediaType, listener: CountingRequestListener
    ): String {
        val response = networkClient.createUploadMetaData(
            prefsHelper.getCsrf(), file.name,
            displayName, folderId, folderLinkId
        ).execute()

        val responseVO = response.body()
        prefsHelper.saveCsrf(responseVO?.csrf)
        val messages: MutableList<String?>? = responseVO?.getMessages()?.toMutableList()
        val recordId = responseVO?.getRecordVO()?.recordId

        if (messages == null || messages.isEmpty()) {
            return context.getString(R.string.upload_record_not_created_error)
        } else if (recordId == null) {
            return messages[0]!!
        }
        uploadFile(file, displayName, mediaType, recordId, messages, listener)

        return STATUS_OK
    }

    override fun uploadFile(
        file: File,
        displayName: String?,
        mediaType: MediaType,
        recordId: Int,
        messages: MutableList<String?>,
        listener: CountingRequestListener
    ) {
        val response = networkClient.uploadFile(file, mediaType, recordId, listener).execute()
        val responseBody = response.body()

        if (responseBody?.string() == STATUS_OK) {
            file.delete()
            messages.add(STATUS_OK)
        }
    }

    override fun startDownloading(
        folderLinkId: Int,
        archiveNr: String,
        archiveId: Int,
        recordId: Int,
        listener: CountingRequestListener
    ) {
        val response = networkClient.getFile(
            prefsHelper.getCsrf(), folderLinkId, archiveNr, archiveId, recordId).execute()
        val downloadData = response.body()?.getDownloadData()
        val downloadURL = downloadData?.downloadURL
        val fileName = downloadData?.fileName
        if (downloadURL != null && fileName != null) {
            downloadFile(downloadURL, getFileOutputStream(fileName), listener)
        }
    }

    override fun downloadFile(
        downloadUrl: String, fileOutputStream: OutputStream, listener: CountingRequestListener) {
        try {
            val response = networkClient.downloadFile(downloadUrl).execute()
            val contentLength = response.body()?.contentLength()
            val inputStream = response.body()?.byteStream()
            if (inputStream != null) {
                try {
                    val updateInterval = 7
                    val data = ByteArray(4 * 1024) // or other buffer size
                    var totalCount = 0L
                    var count: Int
                    var reportedProgress = 0L
                    while (inputStream.read(data).also { count = it } != -1) {
                        totalCount += count
                        fileOutputStream.write(data, 0, count)
                        // Report progress
                        if (contentLength != null && contentLength > 0) {
                            val newProgress = 100 * totalCount / contentLength
                            if (newProgress >= reportedProgress + updateInterval) {
                                reportedProgress = newProgress
                                listener.onProgressUpdate(reportedProgress)
                            }
                        }
                    }
                    fileOutputStream.flush()
                } catch (e: Exception) {
                    Log.e(FileRepositoryImpl::class.java.simpleName, e.message!!)
                    return
                } finally {
                    inputStream.close()
                }
            }
        } catch (e: IOException) {
            Log.e(FileRepositoryImpl::class.java.simpleName, e.message!!)
        } finally {
            fileOutputStream.close()
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

    override fun deleteRecord(
        record: Record,
        listener: IFileRepository.IOnResponseListener
    ) {
        networkClient.deleteRecord(prefsHelper.getCsrf(), record)
            .enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    prefsHelper.saveCsrf(responseVO?.csrf)
                    val firstMessage = responseVO?.getMessages()?.get(0)

                    if (firstMessage != null && (firstMessage == Constants.FILE_DELETED_SUCCESSFULLY
                                || firstMessage == Constants.FOLDER_DELETED_SUCCESSFULLY)) {
                        listener.onSuccess(null)
                    } else {
                        listener.onFailed(context.getString(R.string.generic_error))
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun relocateRecord(
        recordToRelocate: Record, destFolderLinkId: Int, relocationType: RelocationType,
        listener: IFileRepository.IOnResponseListener
    ) {
        networkClient.relocateRecord(
            prefsHelper.getCsrf(), recordToRelocate, destFolderLinkId, relocationType)
            .enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    prefsHelper.saveCsrf(responseVO?.csrf)
                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                        val relocationVerb = if (relocationType == RelocationType.MOVE)
                            context.getString(R.string.relocation_type_moved)
                        else context.getString(R.string.relocation_type_copied)
                        listener.onSuccess(context.getString(R.string.relocation_success,
                            recordToRelocate.type?.name?.toLowerCase()?.capitalize(), relocationVerb))
                    } else {
                        listener.onFailed(context.getString(R.string.generic_error))
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }
}