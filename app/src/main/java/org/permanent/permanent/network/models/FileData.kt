package org.permanent.permanent.network.models

import android.os.Parcel
import android.os.Parcelable
import org.permanent.permanent.models.FileType
import org.permanent.permanent.models.Tag

class FileData private constructor() : Parcelable {
    var recordId: Int? = null
    var folderLinkId: Int? = null
    var archiveNr: String? = null
    var fileName: String? = null
    var displayName: String? = null
    var description: String? = null
    var displayDate: String? = null
    var createdDate: String? = null
    var updatedDate: String? = null
    var derivedDate: String? = null
    var derivedCreatedDate: String? = null
    var size: Long? = null
    var originalFileName: String? = null
    var originalFileType: String? = null
    var fileURL: String? = null
    var downloadURL: String? = null
    var thumbURL2000: String? = null
    var contentType: String? = null
    var width: Int? = null
    var height: Int? = null
    var tags: List<Tag>? = null

    constructor(recordVO: RecordVO) : this() {
        recordId = recordVO.recordId
        folderLinkId = recordVO.folder_linkId
        archiveNr = recordVO.archiveNbr
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
        createdDate = recordVO.createdDT?.replace("T", " ")
        updatedDate = recordVO.updatedDT?.replace("T", " ")
        derivedDate = recordVO.derivedDT?.replace("T", " ")
        derivedCreatedDate = recordVO.derivedCreatedDT?.replace("T", " ")
        size = recordVO.size
        originalFileName = recordVO.uploadFileName?.substringBefore(".")
        originalFileType = recordVO.uploadFileName?.substringAfter(".")
        fileURL = fileVO?.fileURL
        downloadURL = fileVO?.downloadURL
        thumbURL2000 = recordVO.thumbURL2000
        contentType = fileVO?.contentType
        width = fileVO?.width
        height = fileVO?.height
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
        recordId = parcel.readInt()
        folderLinkId = parcel.readInt()
        archiveNr = parcel.readString()
        fileName = parcel.readString()
        displayName = parcel.readString()
        description = parcel.readString()
        displayDate = parcel.readString()
        createdDate = parcel.readString()
        updatedDate = parcel.readString()
        derivedDate = parcel.readString()
        derivedCreatedDate = parcel.readString()
        size = parcel.readLong()
        originalFileName = parcel.readString()
        originalFileType = parcel.readString()
        fileURL = parcel.readString()
        downloadURL = parcel.readString()
        thumbURL2000 = parcel.readString()
        contentType = parcel.readString()
        width = parcel.readInt()
        height = parcel.readInt()
        tags = parcel.createTypedArrayList(Tag)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(recordId ?: -1)
        parcel.writeInt(folderLinkId ?: -1)
        parcel.writeString(archiveNr)
        parcel.writeString(fileName)
        parcel.writeString(displayName)
        parcel.writeString(description)
        parcel.writeString(displayDate)
        parcel.writeString(createdDate)
        parcel.writeString(updatedDate)
        parcel.writeString(derivedDate)
        parcel.writeString(derivedCreatedDate)
        parcel.writeLong(size ?: -1L)
        parcel.writeString(originalFileName)
        parcel.writeString(originalFileType)
        parcel.writeString(fileURL)
        parcel.writeString(downloadURL)
        parcel.writeString(thumbURL2000)
        parcel.writeString(contentType)
        parcel.writeInt(width ?: -1)
        parcel.writeInt(height ?: -1)
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