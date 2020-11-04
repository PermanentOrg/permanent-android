package org.permanent.permanent.repositories

import okhttp3.MediaType
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.ui.myFiles.upload.CountingRequestListener
import java.io.File

interface IFileRepository {
    fun getMyFilesRecord(listener: IOnMyFilesArchiveNrListener)
    fun getChildRecordsOf(myFilesArchiveNr: String, listener: IOnRecordsRetrievedListener)
    fun navigateMin(archiveNumber: String, listener: IOnRecordsRetrievedListener)
    fun getLeanItems(archiveNumber: String, childLinkIds: List<Int>,
                     listener: IOnRecordsRetrievedListener)
    fun createFolder(name: String, listener: IOnFolderCreatedListener)
    fun startUploading(
        file: File, displayName: String?, mediaType: MediaType, listener: CountingRequestListener
    ): String
    fun uploadFile(
        file: File,
        displayName: String?,
        mediaType: MediaType,
        recordId: Int,
        messages: MutableList<String?>,
        listener: CountingRequestListener
    )

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
}