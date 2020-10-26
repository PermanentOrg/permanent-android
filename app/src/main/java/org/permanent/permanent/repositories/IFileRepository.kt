package org.permanent.permanent.repositories

import okhttp3.MediaType
import org.permanent.permanent.network.models.RecordVO
import java.io.File

interface IFileRepository {
    fun getMyFilesRecord(listener: IOnMyFilesArchiveNrListener)
    fun getChildRecordsOf(myFilesArchiveNr: String, listener: IOnRecordsRetrievedListener)
    fun navigateMin(archiveNumber: String, listener: IOnRecordsRetrievedListener)
    fun getLeanItems(archiveNumber: String, childLinkIds: List<Int>,
                     listener: IOnRecordsRetrievedListener)
    fun startUploading(file: File, displayName: String?, mediaType: MediaType): String
    fun uploadFile(
        file: File,
        displayName: String?,
        mediaType: MediaType,
        recordId: Int,
        messages: MutableList<String?>
    )

    interface IOnMyFilesArchiveNrListener {
        fun onSuccess(myFilesRecord: RecordVO)
        fun onFailed(error: String?)
    }

    interface IOnRecordsRetrievedListener {
        fun onSuccess(records: List<RecordVO>?)
        fun onFailed(error: String?)
    }
}