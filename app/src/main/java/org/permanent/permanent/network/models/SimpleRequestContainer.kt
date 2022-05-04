package org.permanent.permanent.network.models

import android.annotation.SuppressLint
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class SimpleRequestContainer {
    private var RequestVO: Request = Request()

    init {
        RequestVO.data = Datum()
    }

    fun addRecord(
        displayName: String?,
        file: File,
        parentFolderId: Int,
        parentFolderLinkId: Int,
        createdDT: Date
    ): SimpleRequestContainer {
        val recordVO = RecordVO()
        recordVO.displayName = displayName
        recordVO.uploadFileName = file.name
        recordVO.size = file.length()
        recordVO.parentFolderId = parentFolderId
        recordVO.parentFolder_linkId = parentFolderLinkId

        val tz = TimeZone.getTimeZone("UTC")
        @SuppressLint("SimpleDateFormat")
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
        df.timeZone = tz
        recordVO.derivedCreatedDT = df.format(createdDT)

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