package org.permanent.permanent.repositories

import org.permanent.permanent.network.models.RecordVO

interface IFileRepository {
    fun getMyFilesRecord(listener: IOnMyFilesArchiveNrListener)
    fun getChildRecordsOf(myFilesArchiveNr: String, listener: IOnRecordsRetrievedListener)
    fun navigateMin(archiveNumber: String, listener: IOnRecordsRetrievedListener)
    fun getLeanItems(archiveNumber: String, childLinks: List<Int>, listener: IOnRecordsRetrievedListener)

    interface IOnMyFilesArchiveNrListener {
        fun onSuccess(myFilesRecord: RecordVO)
        fun onFailed(error: String?)
    }

    interface IOnRecordsRetrievedListener {
        fun onSuccess(records: List<RecordVO>?)
        fun onFailed(error: String?)
    }
}