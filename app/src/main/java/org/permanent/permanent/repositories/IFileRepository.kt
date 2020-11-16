package org.permanent.permanent.repositories

import okhttp3.MediaType
import org.permanent.permanent.models.FolderIdentifier
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.ui.myFiles.upload.CountingRequestListener
import java.io.File
import java.io.OutputStream

interface IFileRepository {
    fun getMyFilesRecord(listener: IOnMyFilesArchiveNrListener)
    fun getChildRecordsOf(myFilesArchiveNr: String, listener: IOnRecordsRetrievedListener)
    fun navigateMin(archiveNumber: String, listener: IOnRecordsRetrievedListener)
    fun getLeanItems(archiveNumber: String, childLinkIds: List<Int>,
                     listener: IOnRecordsRetrievedListener)
    fun createFolder(
        parentFolderIdentifier: FolderIdentifier, name: String, listener: IOnFolderCreatedListener)
    fun startUploading(folderId: Int, folderLinkId: Int, file: File, displayName: String?,
                       mediaType: MediaType, listener: CountingRequestListener): String
    fun uploadFile(
        file: File,
        displayName: String?,
        mediaType: MediaType,
        recordId: Int,
        messages: MutableList<String?>,
        listener: CountingRequestListener
    )
    fun startDownloading(
        folderLinkId: Int,
        archiveNr: String,
        archiveId: Int,
        recordId: Int,
        listener: CountingRequestListener
    )
    fun downloadFile(
        downloadUrl: String, fileOutputStream: OutputStream, listener: CountingRequestListener)
    fun deleteRecord(record: RecordVO, listener: IOnRecordDeletedListener)

    interface IOnMyFilesArchiveNrListener {
        fun onSuccess(myFilesRecord: RecordVO)
        fun onFailed(error: String?)
    }

    interface IOnRecordsRetrievedListener {
        fun onSuccess(records: List<RecordVO>?)
        fun onFailed(error: String?)
    }

    interface IOnFolderCreatedListener {
        fun onSuccess()
        fun onFailed(error: String?)
    }

    interface IOnRecordDeletedListener {
        fun onSuccess()
        fun onFailed(error: String?)
    }
}