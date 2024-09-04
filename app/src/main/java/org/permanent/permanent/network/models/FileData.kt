package org.permanent.permanent.network.models

import android.os.Parcel
import android.os.Parcelable
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.FileType
import org.permanent.permanent.models.Tag

class FileData private constructor() : Parcelable {
    var recordId: Int = -1
    var folderLinkId: Int = -1
    var archiveId: Int = -1
    var archiveNr: String? = null
    var accessRole: AccessRole? = null
    var fileName: String? = null
    var displayName: String? = null
    var description: String? = null
    var displayDate: String? = null
    var createdDate: String? = null
    var updatedDate: String? = null
    var derivedDate: String? = null
    var derivedCreatedDate: String? = null
    var size: Long = -1L
    var originalFileName: String? = null
    var originalFileType: String? = null
    var completeAddress: String? = null
    var latitude: Double = -1.0
    var longitude: Double = -1.0
    var fileURL: String? = null
    var downloadURL: String? = null
    var thumbURL2000: String? = null
    var contentType: String? = null
    var width: Int = -1
    var height: Int = -1
    var tags: List<Tag>? = null

    constructor(recordVO: RecordVO) : this() {
        recordId = recordVO.recordId ?: -1
        folderLinkId = recordVO.folder_linkId ?: -1
        archiveId = recordVO.archiveId ?: -1
        archiveNr = recordVO.archiveNbr
        accessRole = AccessRole.createFromBackendString(recordVO.accessRole)
        // First we check for the converted video to mp4
        val fileVO: FileVO? = if (recordVO.type?.contains(FileType.VIDEO.toString()) == true
            && recordVO.FileVOs?.size!! > 1
        ) {
            fileName = recordVO.displayName + ".mp4"
            recordVO.FileVOs?.get(1)
        } else {
            fileName = recordVO.uploadFileName
            recordVO.FileVOs?.get(0)
        }
        displayName = recordVO.displayName
        description = recordVO.description
        displayDate = recordVO.displayDT
        createdDate = recordVO.createdDT?.replace("T", " ")
        updatedDate = recordVO.updatedDT?.replace("T", " ")
        derivedDate = recordVO.derivedDT?.replace("T", " ")
        derivedCreatedDate = recordVO.derivedCreatedDT?.replace("T", " ")
        size = recordVO.size ?: -1L
        originalFileName = recordVO.uploadFileName?.substringBefore(".")
        originalFileType = recordVO.uploadFileName?.substringAfter(".")

        completeAddress = recordVO.LocnVO?.let {
            val streetName = if (it.streetName == null) "" else it.streetName + ", "
            val addressValue =
                (it.streetNumber ?: "") + " " + streetName + it.locality +
                        ", " + it.adminOneName + ", " + it.countryCode
            if (!addressValue.contains("null")) addressValue else ""
        }
        latitude = recordVO.LocnVO?.latitude ?: -1.0
        longitude = recordVO.LocnVO?.longitude ?: -1.0

        fileURL = fileVO?.fileURL
        downloadURL = fileVO?.downloadURL
        thumbURL2000 = recordVO.thumbURL2000
        contentType = fileVO?.contentType
        width = fileVO?.width ?: -1
        height = fileVO?.height ?: -1
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
        archiveId = parcel.readInt()
        archiveNr = parcel.readString()
        accessRole = parcel.readParcelable(AccessRole::class.java.classLoader)
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
        completeAddress = parcel.readString()
        latitude = parcel.readDouble()
        longitude = parcel.readDouble()
        fileURL = parcel.readString()
        downloadURL = parcel.readString()
        thumbURL2000 = parcel.readString()
        contentType = parcel.readString()
        width = parcel.readInt()
        height = parcel.readInt()
        tags = parcel.createTypedArrayList(Tag)
    }

    fun update(locationVO: LocnVO) {
        val streetName = if (locationVO.streetName == null) "" else locationVO.streetName + ", "
        val addressValue = (locationVO.streetNumber ?: "") + " " + streetName +
                locationVO.locality + ", " + locationVO.adminOneName + ", " + locationVO.countryCode
        completeAddress = if (!addressValue.contains("null")) addressValue else ""
        latitude = locationVO.latitude ?: -1.0
        longitude = locationVO.longitude ?: -1.0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(recordId)
        parcel.writeInt(folderLinkId)
        parcel.writeInt(archiveId)
        parcel.writeString(archiveNr)
        parcel.writeParcelable(accessRole, flags)
        parcel.writeString(fileName)
        parcel.writeString(displayName)
        parcel.writeString(description)
        parcel.writeString(displayDate)
        parcel.writeString(createdDate)
        parcel.writeString(updatedDate)
        parcel.writeString(derivedDate)
        parcel.writeString(derivedCreatedDate)
        parcel.writeLong(size)
        parcel.writeString(originalFileName)
        parcel.writeString(originalFileType)
        parcel.writeString(completeAddress)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeString(fileURL)
        parcel.writeString(downloadURL)
        parcel.writeString(thumbURL2000)
        parcel.writeString(contentType)
        parcel.writeInt(width)
        parcel.writeInt(height)
        parcel.writeTypedList(tags)
    }

    fun getTagIds(): ArrayList<String> {
        val resultList = ArrayList<String>()
        tags?.let {
            for (tag in it) {
                tag.tagId?.let { tagId -> resultList.add(tagId) }
            }
        }
        return resultList
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