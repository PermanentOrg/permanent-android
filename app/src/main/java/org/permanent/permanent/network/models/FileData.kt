package org.permanent.permanent.network.models

import android.os.Parcel
import android.os.Parcelable
import org.permanent.permanent.models.FileType
import org.permanent.permanent.models.Tag

class FileData private constructor() : Parcelable {
    var displayName: String? = null
    var description: String? = null
    var displayDate: String? = null
    var fileURL: String? = null
    var downloadURL: String? = null
    var thumbURL2000: String? = null
    var contentType: String? = null
    var fileName: String? = null
    var tags: List<Tag>? = null

    constructor(recordVO: RecordVO) : this() {
        // First we check for the converted video to mp4
        val fileVO: FileVO? = if (recordVO.type?.contains(FileType.VIDEO.toString()) == true
            && recordVO.FileVOs?.size!! > 1) {
            fileName = recordVO.displayName + ".mp4"
            recordVO.FileVOs?.get(1)
        } else {
            fileName = recordVO.uploadFileName
            recordVO.FileVOs?.get(0)
        }
        displayName = recordVO.displayName
        description = recordVO.description
        displayDate = recordVO.displayDT?.substringBefore("T")
        fileURL = fileVO?.fileURL
        downloadURL = fileVO?.downloadURL
        thumbURL2000 = recordVO.thumbURL2000
        contentType = fileVO?.contentType
        initTags(recordVO.TagVOs)
    }

    private fun initTags(tagVOs: List<TagVO>?) {
        tags = ArrayList()
        tagVOs?.let {
            for (tagVO in it) {
                (tags as ArrayList<Tag>).add(Tag(tagVO))
            }
        }
    }

    constructor(parcel: Parcel) : this() {
        displayName = parcel.readString()
        description = parcel.readString()
        displayDate = parcel.readString()
        fileURL = parcel.readString()
        downloadURL = parcel.readString()
        thumbURL2000 = parcel.readString()
        contentType = parcel.readString()
        fileName = parcel.readString()
        tags = parcel.createTypedArrayList(Tag)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(displayName)
        parcel.writeString(description)
        parcel.writeString(displayDate)
        parcel.writeString(fileURL)
        parcel.writeString(downloadURL)
        parcel.writeString(thumbURL2000)
        parcel.writeString(contentType)
        parcel.writeString(fileName)
        parcel.writeTypedList(tags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FileData> {
        override fun createFromParcel(parcel: Parcel): FileData {
            return FileData(parcel)
        }

        override fun newArray(size: Int): Array<FileData?> {
            return arrayOfNulls(size)
        }
    }
}