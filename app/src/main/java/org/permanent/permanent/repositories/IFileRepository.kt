package org.permanent.permanent.repositories

import okhttp3.MediaType
import org.permanent.permanent.models.FolderIdentifier
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.ShareRequestType
import org.permanent.permanent.network.models.Shareby_urlVO
import org.permanent.permanent.ui.myFiles.RelocationType
import org.permanent.permanent.ui.myFiles.upload.CountingRequestListener
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
        parentFolderIdentifier: FolderIdentifier, name: String, listener: IResponseListener)
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
    fun deleteRecord(record: Record, listener: IResponseListener)
    fun relocateRecord(recordToRelocate: Record, destFolderLinkId: Int,
                       relocationType: RelocationType, listener: IResponseListener)
    fun requestShareLink(record: Record, shareRequestType: ShareRequestType,
                         listener: IOnShareUrlListener)
    fun modifyShareLink(shareVO: Shareby_urlVO, shareRequestType: ShareRequestType,
                        listener: IResponseListener)
    fun getShares(listener: IDataListener)

    interface IOnMyFilesArchiveNrListener {
        fun onSuccess(myFilesRecord: Record)
        fun onFailed(error: String?)
    }

    interface IOnRecordsRetrievedListener {
        fun onSuccess(records: List<Record>?)
        fun onFailed(error: String?)
    }

    interface IOnShareUrlListener {
        fun onSuccess(shareByUrlVO: Shareby_urlVO?)
        fun onFailed(error: String?)
    }
}