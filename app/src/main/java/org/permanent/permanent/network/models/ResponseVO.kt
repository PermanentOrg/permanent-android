package org.permanent.permanent.network.models

import org.permanent.permanent.Constants
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType

class ResponseVO {
    var Results: List<ResultVO>? = null
    var isSuccessful: Boolean? = false
    var csrf: String? = null

    fun isUserLoggedIn(): Boolean? {
        return Results?.get(0)?.data?.get(0)?.SimpleVO?.value
    }

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

    fun getRecordVO(): RecordVO? {
        return Results?.get(0)?.data?.get(0)?.RecordVO
    }

    fun getChildItemVOs(): List<RecordVO>? {
        return Results?.get(0)?.data?.get(0)?.FolderVO?.ChildItemVOs
    }

    fun getRecords(): List<Record> {
        val records = ArrayList<Record>()
        val recordVOs: List<RecordVO>? = getChildItemVOs()

        if (recordVOs != null) {
            for (recordVO in recordVOs) {
                val newRecord: Record
                if (recordVO.folderId != null) {
                    recordVO.id = recordVO.folderId
                    newRecord = Record(recordVO)
                    newRecord.type = RecordType.FOLDER
                } else {
                    recordVO.id = recordVO.recordId
                    newRecord = Record(recordVO)
                    newRecord.type = RecordType.FILE
                }
                records.add(newRecord)
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
                val downloadDatum = DownloadData()
                downloadDatum.displayName = datum?.RecordVO!!.displayName
                downloadDatum.downloadURL = fileVO?.downloadURL
                downloadDatum.contentType = fileVO?.contentType
                downloadDatum.fileName = datum.RecordVO!!.uploadFileName
                return downloadDatum
            }
        }
        return null
    }
}
