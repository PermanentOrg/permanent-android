package org.permanent.permanent.repositories

import okhttp3.MediaType
import okhttp3.ResponseBody
import org.permanent.permanent.models.NavigationFolderIdentifier
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.network.models.UploadDestination
import org.permanent.permanent.network.models.GetPresignedUrlResponse
import org.permanent.permanent.ui.myFiles.RelocationType
import org.permanent.permanent.ui.myFiles.upload.CountingRequestListener
import retrofit2.Call
import java.io.File
import java.io.OutputStream

interface IFileRepository {
    fun getMyFilesRecord(listener: IOnMyFilesArchiveNrListener)

    fun getChildRecordsOf(myFilesArchiveNr: String, sort: String?,
                          listener: IOnRecordsRetrievedListener)

    fun navigateMin(archiveNumber: String, sort: String?, listener: IOnRecordsRetrievedListener)

    fun getLeanItems(archiveNumber: String, sort: String?, childLinkIds: List<Int>,
                     listener: IOnRecordsRetrievedListener)
    fun createFolder(
        parentFolderIdentifier: NavigationFolderIdentifier, name: String, listener: IResponseListener)

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
        folderLinkId: Int,
        archiveNr: String,
        archiveId: Int,
        recordId: Int,
    ): Call<ResponseVO>

    fun downloadFile(
        downloadUrl: String, fileOutputStream: OutputStream, listener: CountingRequestListener)

    fun deleteRecord(record: Record, listener: IResponseListener)

    fun relocateRecord(recordToRelocate: Record, destFolderLinkId: Int,
                       relocationType: RelocationType, listener: IResponseListener)

    interface IOnMyFilesArchiveNrListener {
        fun onSuccess(myFilesRecord: Record)
        fun onFailed(error: String?)
    }

    interface IOnRecordsRetrievedListener {
        fun onSuccess(records: List<Record>?)
        fun onFailed(error: String?)
    }
}