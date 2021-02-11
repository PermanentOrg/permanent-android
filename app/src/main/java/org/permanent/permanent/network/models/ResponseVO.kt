package org.permanent.permanent.network.models

import org.permanent.permanent.Constants
import org.permanent.permanent.models.Record

class ResponseVO {
    var Results: List<ResultVO>? = null
    var isSuccessful: Boolean? = false
    var csrf: String? = null

    fun getMyFilesRecord(): Record? {
        val recordVOs: List<RecordVO>? = getChildItemVOs()

        if (recordVOs != null) {
            for (recordVO in recordVOs) {
                if (recordVO.displayName.equals(Constants.MY_FILES_FOLDER)) {
                    return Record(recordVO)
                }
            }
        }

        return null
    }

    fun getData(): List<Datum>? {
        return Results?.get(0)?.data
    }

    fun isUserLoggedIn(): Boolean? {
        return getData()?.get(0)?.SimpleVO?.value as Boolean?
    }

    fun getArchiveId(): Int? {
        return getData()?.get(0)?.FolderVO?.archiveId
    }

    fun getArchiveNr(): String? {
        return getData()?.get(0)?.FolderVO?.archiveNbr
    }

    fun getAccount(): AccountVO? {
        return getData()?.get(0)?.AccountVO
    }

    fun getShareByUrlVO(): Shareby_urlVO? {
        return getData()?.get(0)?.Shareby_urlVO
    }

    fun getShareVO(): ShareVO? {
        return getData()?.get(0)?.ShareVO
    }

    fun getChildItemVOs(): List<RecordVO>? {
        return getData()?.get(0)?.FolderVO?.ChildItemVOs
    }

    fun getRecords(): List<Record> {
        val records = ArrayList<Record>()
        val recordVOs: List<RecordVO>? = getChildItemVOs()

        if (recordVOs != null) {
            for (recordVO in recordVOs) {
                records.add(Record(recordVO))
            }
        }

        return records
    }

    fun getMessages(): List<String?>? {
        return Results?.get(0)?.message
    }

    fun getDownloadData(): DownloadData? {
        if (!Results.isNullOrEmpty()) {
            for (result in Results!!) {
                val datum = result.data?.get(0)
                val fileVO: FileVO? = datum?.RecordVO?.FileVOs?.get(0)
                return DownloadData(datum, fileVO)
            }
        }
        return null
    }
}
