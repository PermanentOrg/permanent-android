package org.permanent.permanent.repositories

import okhttp3.MediaType
import okhttp3.ResponseBody
import org.permanent.permanent.models.NavigationFolderIdentifier
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.Tag
import org.permanent.permanent.network.IFileDataListener
import org.permanent.permanent.network.IRecordListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.FileData
import org.permanent.permanent.network.models.GetPresignedUrlResponse
import org.permanent.permanent.network.models.IFolderChildrenListener
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

    fun getMyFilesRecordV2(isStale: () -> Boolean, listener: IRecordListener)

    fun getPublicRoot(archiveNr: String?, listener: IRecordListener)

    fun getPublicRootV2(isStale: () -> Boolean, listener: IRecordListener)

    fun getChildRecordsOf(
        folderArchiveNr: String,
        folderLinkId: Int,
        sort: String?,
        listener: IOnRecordsRetrievedListener
    )

    fun getChildRecordsOfV2(folderId: Int, listener: IFolderChildrenListener)

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

    fun getFolderV2(folderId: Int, folderLinkId: Int, listener: IRecordListener)

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

    // Record detail behind use_stela_migration with the V1 record/get failsafe; a foreign
    // record rides V2 only when the caller vouches it is public (gallery).
    fun getFileData(
        recordId: Int,
        folderLinkId: Int,
        archiveId: Int?,
        allowsForeignPublic: Boolean,
        listener: IFileDataListener
    )

    // Same routing for callers on a worker thread; a V1 network failure propagates as before.
    fun getFileDataBlocking(
        recordId: Int, folderLinkId: Int, archiveId: Int?, allowsForeignPublic: Boolean
    ): FileData?

    fun getRecordV2(recordId: Int, folderLinkId: Int, listener: IRecordListener)

    fun downloadFile(downloadUrl: String): Call<ResponseBody>

    fun deleteRecords(records: MutableList<Record>, listener: IResponseListener)

    fun unshareRecord(record: Record, archiveId: Int, listener: IResponseListener)

    fun relocateRecords(
        records: MutableList<Record>,
        destFolderLinkId: Int,
        destFolderId: Int,
        relocationType: ModificationType,
        listener: IResponseListener
    )

    // The fields an edit changed: the V2 write carries only these. DATE has no V2 field.
    enum class RecordField { NAME, DESCRIPTION, DATE }

    fun updateRecords(
        fileDataList: List<FileData?>, fields: Set<RecordField>, listener: IResponseListener
    )

    fun updateRecord(locnVO: LocnVO, fileData: FileData, listener: IResponseListener)

    fun updateRecord(record: Record, newName: String, listener: IResponseListener)

    fun updateMultipleRecords(records: MutableList<Record>, locnVO: LocnVO, listener: IResponseListener)

    fun updateMultipleRecords(records: MutableList<Record>, isFolderRecordType: Boolean, listener: IResponseListener)

    // Bulk rename: each record's displayName is already set to its new value.
    fun renameRecords(records: MutableList<Record>, listener: IResponseListener)

    fun searchRecords(query: String?, tags: List<Tag>, listener: IOnRecordsRetrievedListener)

    interface IOnRecordsRetrievedListener {
        fun onSuccess(parentFolderName: String?, recordVOs: List<RecordVO>?)
        fun onFailed(error: String?)
    }
}