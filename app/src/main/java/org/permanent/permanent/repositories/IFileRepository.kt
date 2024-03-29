package org.permanent.permanent.repositories

import okhttp3.MediaType
import okhttp3.ResponseBody
import org.permanent.permanent.models.NavigationFolderIdentifier
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.Tag
import org.permanent.permanent.network.IRecordListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.FileData
import org.permanent.permanent.network.models.GetPresignedUrlResponse
import org.permanent.permanent.network.models.LocnVO
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.network.models.UploadDestination
import org.permanent.permanent.ui.myFiles.ModificationType
import org.permanent.permanent.ui.myFiles.upload.CountingRequestListener
import retrofit2.Call
import java.io.File
import java.util.Date

interface IFileRepository {
    fun getMyFilesRecord(listener: IRecordListener)

    fun getPublicRoot(archiveNr: String?, listener: IRecordListener)

    fun getChildRecordsOf(
        folderArchiveNr: String,
        folderLinkId: Int,
        sort: String?,
        listener: IOnRecordsRetrievedListener
    )

    fun navigateMin(
        archiveNr: String, folderLinkId: Int, sort: String?, listener: IOnRecordsRetrievedListener
    )

    fun getLeanItems(
        archiveNr: String,
        folderLinkId: Int,
        sort: String?,
        childLinkIds: List<Int>,
        listener: IOnRecordsRetrievedListener
    )

    fun updateProfileBanner(thumbRecord: Record, listener: IResponseListener)

    fun createFolder(
        parentFolderIdentifier: NavigationFolderIdentifier, name: String, listener: IRecordListener
    )

    fun getFolder(
        folderLinkId: Int, listener: IRecordListener
    )

    fun getPresignedUrlForUpload(
        folderId: Int, folderLinkId: Int, file: File, displayName: String, mediaType: MediaType
    ): Call<GetPresignedUrlResponse>

    fun uploadFile(
        file: File,
        mediaType: MediaType,
        uploadDestination: UploadDestination,
        listener: CountingRequestListener
    ): Call<ResponseBody>?

    fun registerRecord(
        folderId: Int,
        folderLinkId: Int,
        file: File,
        displayName: String,
        createdDT: Date,
        s3Url: String
    ): Call<ResponseVO>

    fun getRecord(
        folderLinkId: Int,
        recordId: Int?,
    ): Call<ResponseVO>

    fun getRecord(
        fileArchiveNr: String,
    ): Call<ResponseVO>

    fun downloadFile(downloadUrl: String): Call<ResponseBody>

    fun deleteRecords(records: MutableList<Record>, listener: IResponseListener)

    fun unshareRecord(record: Record, archiveId: Int, listener: IResponseListener)

    fun relocateRecords(
        records: MutableList<Record>,
        destFolderLinkId: Int,
        relocationType: ModificationType,
        listener: IResponseListener
    )

    fun updateRecords(fileDataList: List<FileData?>, listener: IResponseListener)

    fun updateRecord(locnVO: LocnVO, fileData: FileData, listener: IResponseListener)

    fun updateRecord(record: Record, newName: String, listener: IResponseListener)

    fun updateMultipleRecords(records: MutableList<Record>, isFolderRecordType: Boolean, listener: IResponseListener)

    fun searchRecords(query: String?, tags: List<Tag>, listener: IOnRecordsRetrievedListener)

    interface IOnRecordsRetrievedListener {
        fun onSuccess(parentFolderName: String?, recordVOs: List<RecordVO>?)
        fun onFailed(error: String?)
    }
}