package org.permanent.permanent.network.models

import org.permanent.permanent.Constants

class ResponseVO {
    var Results: List<ResultVO>? = null
    var isSuccessful: Boolean? = false
    var csrf: String? = null

    fun isUserLoggedIn(): Boolean? {
        return Results?.get(0)?.data?.get(0)?.SimpleVO?.value
    }

    fun getMyFilesRecordVO(): RecordVO? {
        val recordVOs: List<RecordVO>? = getChildItemVOs()

        if (recordVOs != null) {
            for (recordVO in recordVOs) {
                if (recordVO.displayName.equals(Constants.MY_FILES_FOLDER)) {
                    return recordVO
                }
            }
        }

        return null
    }

    fun getRecordVO(): RecordVO? {
        return Results?.get(0)?.data?.get(0)?.RecordVO
    }

    fun getChildItemVOs(): List<RecordVO>? {
        return getFolderVO()?.ChildItemVOs
    }

    fun getFolderVO(): FolderVO? {
        return Results?.get(0)?.data?.get(0)?.FolderVO
    }

    fun getRecordVOs(): List<RecordVO>? {
        val recordVOs = getChildItemVOs()

        if (recordVOs != null) {
            for (recordVO in recordVOs) {
                if (recordVO.folderId != null) {
                    recordVO.id = recordVO.folderId
                    recordVO.typeEnum = RecordVO.Type.Folder
                } else {
                    recordVO.id = recordVO.recordId
                    recordVO.typeEnum = RecordVO.Type.Image
                }
            }
        }

        return recordVOs
    }

    fun getMessages(): List<String?>? {
        return Results?.get(0)?.message
    }
}
