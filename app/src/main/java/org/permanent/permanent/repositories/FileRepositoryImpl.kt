package org.permanent.permanent.repositories

import android.content.Context
import android.content.SharedPreferences
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.permanent.permanent.R
import org.permanent.permanent.models.NavigationFolderIdentifier
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.models.Tag
import org.permanent.permanent.network.IRecordListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.models.*
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.myFiles.RelocationType
import org.permanent.permanent.ui.myFiles.upload.CountingRequestListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*

class FileRepositoryImpl(val context: Context) : IFileRepository {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val prefsHelper = PreferencesHelper(sharedPreferences)

    override fun getMyFilesRecord(listener: IRecordListener) {
        NetworkClient.instance().getRoot().enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()
                val publicRecord = responseVO?.getPublicRecord()
                prefsHelper.savePublicRecordInfo(
                    publicRecord?.folderId,
                    publicRecord?.folderLinkId,
                    publicRecord?.archiveNr,
                    publicRecord?.thumbURL2000
                )
                val myFilesRecord = responseVO?.getMyFilesRecord()

                if (myFilesRecord != null) {
                    listener.onSuccess(myFilesRecord)
                } else {
                    listener.onFailed(
                        responseVO?.Results?.get(0)?.message?.get(0) ?: response.errorBody()
                            ?.toString()
                    )
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun getPublicRoot(archiveNr: String?, listener: IRecordListener) {
        NetworkClient.instance().getPublicRootForArchive(archiveNr)
            .enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    val publicRecord = responseVO?.getFolderRecord()

                    if (publicRecord != null) {
                        listener.onSuccess(publicRecord)
                    } else {
                        listener.onFailed(
                            responseVO?.Results?.get(0)?.message?.get(0) ?: response.errorBody()
                                ?.toString()
                        )
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun getChildRecordsOf(
        folderArchiveNr: String,
        folderLinkId: Int,
        sort: String?,
        listener: IFileRepository.IOnRecordsRetrievedListener
    ) {
        navigateMin(folderArchiveNr, folderLinkId, sort, listener)
    }

    override fun navigateMin(
        archiveNr: String,
        folderLinkId: Int,
        sort: String?,
        listener: IFileRepository.IOnRecordsRetrievedListener
    ) {
        NetworkClient.instance().navigateMin(archiveNr, folderLinkId)
            .enqueue(object : Callback<ResponseVO> {
                override fun onResponse(
                    call: Call<ResponseVO>, response: Response<ResponseVO>
                ) {
                    val folderLinkIds: MutableList<Int> = ArrayList()
                    val recordVOs: List<RecordVO?>? = response.body()?.getRecordVOs()

                    if (recordVOs != null) {
                        for (recordVO in recordVOs) {
                            recordVO?.folder_linkId?.let { folderLinkIds.add(it) }
                        }
                        getLeanItems(archiveNr, folderLinkId, sort, folderLinkIds, listener)
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun getLeanItems(
        archiveNr: String,
        folderLinkId: Int,
        sort: String?,
        childLinkIds: List<Int>,
        listener: IFileRepository.IOnRecordsRetrievedListener
    ) {
        NetworkClient.instance().getLeanItems(archiveNr, folderLinkId, sort, childLinkIds)
            .enqueue(object : Callback<ResponseVO> {

                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    listener.onSuccess(
                        responseVO?.getFolderRecord()?.displayName, responseVO?.getRecordVOs()
                    )
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun updateProfileBanner(thumbRecord: Record, listener: IResponseListener) {
        thumbRecord.archiveNr?.let {
            NetworkClient.instance().updateProfileBanner(
                prefsHelper.getPublicRecordFolderId(),
                prefsHelper.getPublicRecordFolderLinkId(),
                prefsHelper.getPublicRecordArchiveNr(),
                it
            ).enqueue(object : Callback<ResponseVO> {

                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    prefsHelper.updatePublicRecordThumbURL(thumbRecord.thumbURL2000)
                    listener.onSuccess("")
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
        }
    }

    override fun createFolder(
        parentFolderIdentifier: NavigationFolderIdentifier, name: String, listener: IRecordListener
    ) {
        NetworkClient.instance().createFolder(
            name, parentFolderIdentifier.folderId, parentFolderIdentifier.folderLinkId
        ).enqueue(object : Callback<ResponseVO> {

            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()
                val firstMessage = responseVO?.getMessages()?.get(0)

                if (firstMessage != null && firstMessage.startsWith("New folder")) {
                    val record = responseVO.getFolderRecord()
                    if (record != null) listener.onSuccess(record)
                } else listener.onFailed(firstMessage)
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun getFolder(folderLinkId: Int, listener: IRecordListener) {
        NetworkClient.instance().getFolder(
            folderLinkId,
        ).enqueue(object : Callback<ResponseVO> {

            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()
                val record = responseVO?.getFolderRecord()
                if (record != null) listener.onSuccess(record)
                else listener.onFailed(responseVO?.getMessages()?.get(0))
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun getPresignedUrlForUpload(
        folderId: Int, folderLinkId: Int, file: File, displayName: String, mediaType: MediaType
    ): Call<GetPresignedUrlResponse> {
        return NetworkClient.instance().getPresignedUrlForUpload(
            file, displayName, folderId, folderLinkId, mediaType
        )
    }

    override fun uploadFile(
        file: File,
        mediaType: MediaType,
        uploadDestination: UploadDestination,
        listener: CountingRequestListener
    ): Call<ResponseBody> {
        return NetworkClient.instance().uploadFile(file, mediaType, uploadDestination, listener)
    }

    override fun registerRecord(
        folderId: Int,
        folderLinkId: Int,
        file: File,
        displayName: String,
        createdDT: Date,
        s3Url: String
    ): Call<ResponseVO> {
        return NetworkClient.instance().registerRecord(
            file, displayName, folderId, folderLinkId, createdDT, s3Url
        )
    }

    override fun getRecord(folderLinkId: Int, recordId: Int?): Call<ResponseVO> {
        return NetworkClient.instance().getRecord(folderLinkId, recordId)
    }

    override fun getRecord(fileArchiveNr: String): Call<ResponseVO> {
        return NetworkClient.instance().getRecord(fileArchiveNr)
    }

    override fun downloadFile(downloadUrl: String): Call<ResponseBody> {
        return NetworkClient.instance().downloadFile(downloadUrl)
    }

    override fun unshareRecord(
        record: Record, archiveId: Int, listener: IResponseListener
    ) {
        NetworkClient.instance().unshareRecord(record, archiveId)
            .enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    val isSuccessful = responseVO?.isSuccessful

                    if (isSuccessful == true) {
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

    override fun deleteRecords(
        records: MutableList<Record>, listener: IResponseListener
    ) {
        var areFilesReady = false
        var areFoldersReady = false
        val folders = getToRelocate(RecordType.FOLDER, records)
        val files = getToRelocate(RecordType.FILE, records)

        if (folders.isNullOrEmpty()) {
            areFoldersReady = true
        } else {
            deleteFilesOrFolders(
                folders,
                object : IResponseListener {
                    override fun onSuccess(message: String?) {
                        areFoldersReady = true
                        if (areFilesReady) listener.onSuccess(message)
                    }

                    override fun onFailed(error: String?) {
                        listener.onFailed(error)
                    }
                }
            )
        }

        if (files.isNullOrEmpty()) {
            areFilesReady = true
        } else {
            deleteFilesOrFolders(
                files,
                object : IResponseListener {
                    override fun onSuccess(message: String?) {
                        areFilesReady = true
                        if (areFoldersReady) listener.onSuccess(message)
                    }

                    override fun onFailed(error: String?) {
                        listener.onFailed(error)
                    }
                }
            )
        }
    }

    private fun deleteFilesOrFolders(records: MutableList<Record>, listener: IResponseListener) {
        NetworkClient.instance().deleteFilesOrFolders(records).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()

                if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
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

    override fun relocateRecords(
        records: MutableList<Record>,
        destFolderLinkId: Int,
        relocationType: RelocationType,
        listener: IResponseListener
    ) {
        var areFilesReady = false
        var areFoldersReady = false
        val folders = getToRelocate(RecordType.FOLDER, records)
        val files = getToRelocate(RecordType.FILE, records)

        if (folders.isNullOrEmpty()) {
            areFoldersReady = true
        } else {
            relocateFilesOrFolders(
                folders,
                destFolderLinkId,
                relocationType,
                object : IResponseListener {
                    override fun onSuccess(message: String?) {
                        areFoldersReady = true
                        if (areFilesReady) listener.onSuccess(message)
                    }

                    override fun onFailed(error: String?) {
                        listener.onFailed(error)
                    }
                }
            )
        }

        if (files.isNullOrEmpty()) {
            areFilesReady = true
        } else {
            relocateFilesOrFolders(
                files,
                destFolderLinkId,
                relocationType,
                object : IResponseListener {
                    override fun onSuccess(message: String?) {
                        areFilesReady = true
                        if (areFoldersReady) listener.onSuccess(message)
                    }

                    override fun onFailed(error: String?) {
                        listener.onFailed(error)
                    }
                }
            )
        }
    }

    private fun getToRelocate(
        recordType: RecordType, records: MutableList<Record>
    ): MutableList<Record> {
        return records.filter { it.type == recordType }.toMutableList()
    }

    private fun relocateFilesOrFolders(
        recordsToRelocate: MutableList<Record>,
        destFolderLinkId: Int,
        relocationType: RelocationType,
        listener: IResponseListener
    ) {
        NetworkClient.instance().relocateFilesOrFolders(
            recordsToRelocate, destFolderLinkId, relocationType
        ).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()
                if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                    val relocationVerb = when (relocationType) {
                        RelocationType.MOVE -> context.getString(R.string.relocation_type_moved)
                        RelocationType.PUBLISH -> context.getString(R.string.relocation_type_published)
                        else -> context.getString(R.string.relocation_type_copied)
                    }
                    listener.onSuccess(
                        context.getString(
                            R.string.relocation_success,
                            recordsToRelocate[0].type?.name?.lowercase(Locale.getDefault())
                                ?.replaceFirstChar {
                                    if (it.isLowerCase()) it.titlecase(
                                        Locale.getDefault()
                                    ) else it.toString()
                                },
                            relocationVerb
                        )
                    )
                } else {
                    listener.onFailed(context.getString(R.string.generic_error))
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun updateRecord(fileData: FileData, listener: IResponseListener) {
        NetworkClient.instance().updateRecord(fileData).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()

                if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                    listener.onSuccess(context.getString(R.string.file_info_update_success))
                } else {
                    listener.onFailed(context.getString(R.string.generic_error))
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun updateRecord(locnVO: LocnVO, fileData: FileData, listener: IResponseListener) {
        NetworkClient.instance().updateRecord(locnVO, fileData)
            .enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()

                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                        listener.onSuccess(context.getString(R.string.file_location_update_success))
                    } else {
                        listener.onFailed(context.getString(R.string.file_location_update_error))
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun updateRecord(record: Record, newName: String, listener: IResponseListener) {
        NetworkClient.instance().updateRecord(record, newName)
            .enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()

                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                        listener.onSuccess(context.getString(R.string.rename_record_rename_success))
                    } else {
                        listener.onFailed(context.getString(R.string.generic_error))
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun searchRecords(
        query: String?, tags: List<Tag>, listener: IFileRepository.IOnRecordsRetrievedListener
    ) {
        NetworkClient.instance().searchRecords(query, tags).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()

                if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                    listener.onSuccess(
                        null, responseVO.getData()?.get(0)?.SearchVO?.ChildItemVOs
                    )
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