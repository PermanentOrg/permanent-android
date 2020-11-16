package org.permanent.permanent.network.models

import android.os.Parcel
import android.os.Parcelable
import org.permanent.permanent.models.FolderIdentifier

class RecordVO() : Parcelable {
    var id: Int? = null
    var displayName: String? = null
    var displayDT: String? = null
    var uploadFileName: String? = null
    var type: String? = null
    var typeEnum: Type? = null
    var isFolder: Boolean? = null
    var dataStatus: Int? = null
    var isRecord: Boolean? = null
    var isFetching: Boolean? = null
    var parentFolderId: Int? = null
    var parentFolder_linkId: Int? = null
    var folder_linkId: Int? = null
    var recordId: Int? = null
    var folderId: Int? = null
    var archiveId: Int? = null
    var archiveNbr: String? = null
    var thumbURL200: String? = null
    var thumbURL500: String? = null
    var thumbURL1000: String? = null
    var thumbURL2000: String? = null
    var FileVOs: List<FileVO>? = null

    constructor(parcel: Parcel) : this() {
        id = parcel.readValue(Int::class.java.classLoader) as? Int
        displayName = parcel.readString()
        displayDT = parcel.readString()
        uploadFileName = parcel.readString()
        type = parcel.readString()
        isFolder = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        dataStatus = parcel.readValue(Int::class.java.classLoader) as? Int
        isRecord = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        isFetching = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        parentFolderId = parcel.readValue(Int::class.java.classLoader) as? Int
        parentFolder_linkId = parcel.readValue(Int::class.java.classLoader) as? Int
        folder_linkId = parcel.readValue(Int::class.java.classLoader) as? Int
        recordId = parcel.readValue(Int::class.java.classLoader) as? Int
        folderId = parcel.readValue(Int::class.java.classLoader) as? Int
        archiveId = parcel.readValue(Int::class.java.classLoader) as? Int
        archiveNbr = parcel.readString()
        thumbURL200 = parcel.readString()
        thumbURL500 = parcel.readString()
        thumbURL1000 = parcel.readString()
        thumbURL2000 = parcel.readString()
    }

    fun getDate(): String? {
        return displayDT?.substringBefore("T")
    }

    fun getFolderIdentifier(): FolderIdentifier? {
        if (folderId != null && folder_linkId != null)
            return FolderIdentifier(folderId!!, folder_linkId!!)
        return null
    }

    enum class Type {
        File, Folder
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(displayName)
        parcel.writeString(displayDT)
        parcel.writeString(uploadFileName)
        parcel.writeString(type)
        parcel.writeValue(isFolder)
        parcel.writeValue(dataStatus)
        parcel.writeValue(isRecord)
        parcel.writeValue(isFetching)
        parcel.writeValue(parentFolderId)
        parcel.writeValue(parentFolder_linkId)
        parcel.writeValue(folder_linkId)
        parcel.writeValue(recordId)
        parcel.writeValue(folderId)
        parcel.writeValue(archiveId)
        parcel.writeString(archiveNbr)
        parcel.writeString(thumbURL200)
        parcel.writeString(thumbURL500)
        parcel.writeString(thumbURL1000)
        parcel.writeString(thumbURL2000)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RecordVO> {
        override fun createFromParcel(parcel: Parcel): RecordVO {
            return RecordVO(parcel)
        }

        override fun newArray(size: Int): Array<RecordVO?> {
            return arrayOfNulls(size)
        }
    }
}