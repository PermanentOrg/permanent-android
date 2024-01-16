package org.permanent.permanent.network.models

import org.permanent.permanent.Constants
import org.permanent.permanent.models.Record

class ResponseVO {
    var paymentIntent: String? = null
    var Results: List<ResultVO>? = null
    var isSuccessful: Boolean? = false

    fun getMyFilesRecord(): Record? {
        val recordVOs: List<RecordVO>? = getRecordVOs()

        if (recordVOs != null) {
            for (recordVO in recordVOs) {
                if (recordVO.displayName.equals(Constants.MY_FILES_FOLDER)) {
                    return Record(recordVO)
                }
            }
        }

        return null
    }

    fun getPublicRecord(): Record? {
        val recordVOs: List<RecordVO>? = getRecordVOs()

        if (recordVOs != null) {
            for (recordVO in recordVOs) {
                if (recordVO.displayName.equals(Constants.PUBLIC_FILES_FOLDER)) {
                    return Record(recordVO)
                }
            }
        }

        return null
    }

    fun getData(): List<Datum>? {
        return Results?.get(0)?.data
    }

    fun getDataFromResults(): List<Datum>? {
        return Results?.map { it.data }?.flatMap { it!! }
    }

    fun getSimpleVO(): SimpleVO? {
        return getData()?.get(0)?.SimpleVO
    }

    fun getAuthSimpleVO(): AuthSimpleVO? {
        return getData()?.get(0)?.AuthSimpleVO
    }

    fun getAccountVO(): AccountVO? {
        return getData()?.get(0)?.AccountVO
    }

    fun getArchiveVO(): ArchiveVO? {
        return getData()?.get(0)?.ArchiveVO
    }

    fun getShareByUrlVO(): Shareby_urlVO? {
        return getData()?.get(0)?.Shareby_urlVO
    }

    fun getShareVO(): ShareVO? {
        return getData()?.get(0)?.ShareVO
    }

    fun getRecordVOs(): List<RecordVO>? {
        return getData()?.get(0)?.FolderVO?.ChildItemVOs
    }

    fun getRecord(): Record? {
        val recordVO = getData()?.get(0)?.RecordVO
        if (recordVO != null) return Record(recordVO)
        return null
    }

    fun getFolderRecord(): Record? {
        val folderVO = getData()?.get(0)?.FolderVO
        if (folderVO != null) return Record(folderVO)
        return null
    }

    fun getMessages(): List<String?>? {
        return Results?.get(0)?.message
    }

    fun getFileData(): FileData? {
        val recordVO = getData()?.get(0)?.RecordVO
        if (recordVO != null) return FileData(recordVO)
        return null
    }

    fun getLocationVO(): LocnVO? {
        return getData()?.get(0)?.LocnVO
    }

    fun getProfileItemVO(): Profile_itemVO? {
        return getData()?.get(0)?.Profile_itemVO
    }

    fun getPromoVO(): PromoVO? {
        return getData()?.get(0)?.PromoVO
    }
}
