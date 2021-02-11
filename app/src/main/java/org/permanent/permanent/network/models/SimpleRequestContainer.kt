package org.permanent.permanent.network.models

import org.permanent.permanent.BuildEnvOption
import org.permanent.permanent.Constants
import org.permanent.permanent.network.PERM_API_KEY_MOBILE_PROD
import org.permanent.permanent.network.PERM_API_KEY_MOBILE_STAGING
import java.io.File

class SimpleRequestContainer(csrf: String?) {
    private var RequestVO: Request = Request()

    init {
        if (Constants.BUILD_ENV === BuildEnvOption.STAGING) {
            RequestVO.apiKey = PERM_API_KEY_MOBILE_STAGING
        } else {
            RequestVO.apiKey = PERM_API_KEY_MOBILE_PROD
        }
        RequestVO.csrf = csrf
        RequestVO.data = Datum()
    }

    fun addRecord(
        displayName: String?,
        file: File,
        parentFolderId: Int,
        parentFolderLinkId: Int
    ): SimpleRequestContainer {
        val recordVO = RecordVO()
        recordVO.displayName = displayName
        recordVO.uploadFileName = file.name
        recordVO.size = file.length()
        recordVO.parentFolderId = parentFolderId
        recordVO.parentFolder_linkId = parentFolderLinkId
        RequestVO.data?.RecordVO = recordVO
        return this
    }

    fun addSimple(s3Url: String): SimpleRequestContainer {
        val simpleVO = SimpleVO()
        simpleVO.key = "s3url"
        simpleVO.value = s3Url
        RequestVO.data?.SimpleVO = simpleVO
        return this
    }
}