package org.permanent.permanent.repositories

import okhttp3.MediaType
import okhttp3.ResponseBody
import org.permanent.permanent.models.NavigationFolderIdentifier
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.*
import org.permanent.permanent.ui.myFiles.RelocationType
import org.permanent.permanent.ui.myFiles.upload.CountingRequestListener
import retrofit2.Call
import java.io.File
import java.io.OutputStream

interface IFileRepository {
    fun getMyFilesRecord(listener: IOnMyFilesArchiveNrListener)

    fun getChildRecordsOf(folderArchiveNr: String, folderLinkId: Int, sort: String?,
                          listener: IOnRecordsRetrievedListener)

    fun navigateMin(
        archiveNr: String,
        folderLinkId: Int,
        sort: String?,
        listener: IOnRecordsRetrievedListener
    )

    fun getLeanItems(archiveNr: String, folderLinkId: Int, sort: String?, childLinkIds: List<Int>,
                     listener: IOnRecordsRetrievedListener)

    fun createFolder(
        parentFolderIdentifier: NavigationFolderIdentifier,
        name: String,
        listener: IResponseListener
    )

    fun getPresignedUrlForUpload(
        folderId: Int, folderLinkId: Int, file: File, displayName: String, mediaType: MediaType
    ): Call<GetPresignedUrlResponse>

    fun uploadFile(file: File, mediaType: MediaType, uploadDestination: UploadDestination,
                   listener: CountingRequestListener
    ): Call<ResponseBody>?

    fun registerRecord(
        folderId: Int, folderLinkId: Int, file: File, displayName: String, s3Url: String
    ): Call<ResponseVO>

    fun getRecord(
        folderLinkId: Int?,
        recordId: Int?,
    ): Call<ResponseVO>

    fun downloadFile(
        downloadUrl: String, fileOutputStream: OutputStream, listener: CountingRequestListener)

    fun deleteRecord(record: Record, listener: IResponseListener)

    fun relocateRecord(recordToRelocate: Record, destFolderLinkId: Int,
                       relocationType: RelocationType, listener: IResponseListener)

    fun updateRecord(fileData: FileData, listener: IResponseListener)

    fun updateRecord(locnVO: LocnVO, fileData: FileData, listener: IResponseListener)

    interface IOnMyFilesArchiveNrListener {
        fun onSuccess(myFilesRecord: Record)
        fun onFailed(error: String?)
    }

    interface IOnRecordsRetrievedListener {
        fun onSuccess(recordVOs: List<RecordVO>?)
        fun onFailed(error: String?)
    }
}