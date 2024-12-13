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

    fun getData(): List<Datum>? = Results?.get(0)?.data

    fun getDataFromResults(): List<Datum>? = Results?.map { it.data }?.flatMap { it!! }

    fun getSimpleVO(): SimpleVO? = getData()?.get(0)?.SimpleVO

    fun getAuthSimpleVO(): AuthSimpleVO? = getData()?.get(0)?.AuthSimpleVO

    fun getAccountVO(): AccountVO? = getData()?.get(0)?.AccountVO

    fun getArchiveVO(): ArchiveVO? = getData()?.get(0)?.ArchiveVO

    fun getShareByUrlVO(): Shareby_urlVO? = getData()?.get(0)?.Shareby_urlVO

    fun getShareVO(): ShareVO? = getData()?.get(0)?.ShareVO

    fun getRecordVOs(): List<RecordVO>? = getData()?.get(0)?.FolderVO?.ChildItemVOs

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

    fun getMessages(): List<String?>? = Results?.get(0)?.message

    fun getFileData(): FileData? {
        val recordVO = getData()?.get(0)?.RecordVO
        if (recordVO != null) return FileData(recordVO)
        return null
    }

    fun getLocationVO(): LocnVO? = getData()?.get(0)?.LocnVO

    fun getTagVO(): TagVO?  = getData()?.get(0)?.TagVO

    fun getProfileItemVO(): Profile_itemVO? {
        return getData()?.get(0)?.Profile_itemVO
    }

    fun getPromoVO(): PromoVO? {
        return getData()?.get(0)?.PromoVO
    }
}
