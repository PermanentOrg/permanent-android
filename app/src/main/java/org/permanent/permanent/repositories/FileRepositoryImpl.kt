package org.permanent.permanent.repositories

import android.content.Context
import android.content.SharedPreferences
import okhttp3.MediaType
import org.permanent.permanent.R
import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.myFiles.STATUS_OK
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
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
                val myFilesRecord = responseVO?.getMyFilesRecordVO()

                if (myFilesRecord != null) {
                    myFilesRecord.folderId?.let { prefsHelper.saveFolderId(it) }
                    myFilesRecord.folder_linkId?.let { prefsHelper.saveFolderLinkId(it) }
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
        listener: IFileRepository.IOnRecordsRetrievedListener
    ) {
        navigateMin(myFilesArchiveNr, listener)
    }

    override fun navigateMin(
        archiveNumber: String,
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
                        getLeanItems(archiveNumber, folderLinkIds, listener)
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun getLeanItems(
        archiveNumber: String, childLinkIds: List<Int>,
        listener: IFileRepository.IOnRecordsRetrievedListener
    ) {
        networkClient.getLeanItems(prefsHelper.getCsrf(), archiveNumber, childLinkIds)
            .enqueue(object : Callback<ResponseVO> {

                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    prefsHelper.saveCsrf(responseVO?.csrf)
                    val folderVO = responseVO?.getFolderVO()
                    folderVO?.folderId?.let { prefsHelper.saveFolderId(it) }
                    folderVO?.folder_linkId?.let { prefsHelper.saveFolderLinkId(it) }
                    listener.onSuccess(responseVO?.getRecordVOs())
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun startUploading(file: File, displayName: String?, mediaType: MediaType): String {
        val response = networkClient.createUploadMetaData(
            prefsHelper.getCsrf(), file.name,
            displayName, prefsHelper.getFolderId(), prefsHelper.getFolderLinkId()
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
        uploadFile(file, displayName, mediaType, recordId, messages)

        return STATUS_OK
    }


    override fun uploadFile(
        file: File,
        displayName: String?,
        mediaType: MediaType,
        recordId: Int,
        messages: MutableList<String?>
    ) {
        val response = networkClient.uploadFile(file, mediaType, recordId).execute()
        val responseBody = response.body()

        if (responseBody?.string() == STATUS_OK) {
            file.delete()
            messages.add(STATUS_OK)
        }
    }
}