package org.permanent.permanent.repositories

import org.permanent.permanent.network.models.RecordVO

interface IFileRepository {
    fun getRecordVOs(listener: IOnRecordsRetrievedListener)
    fun getRoot(listener: IOnRecordsRetrievedListener)
    fun navigateMin(csrf: String?, archiveNumber: String, listener: IOnRecordsRetrievedListener)
    fun getLeanItems(csrf: String?, archiveNumber: String, childLinks: List<Int>,
                     listener: IOnRecordsRetrievedListener)

    interface IOnRecordsRetrievedListener {
        fun onSuccess(records: List<RecordVO>?)
        fun onFailed(error: String?)
    }
}