package org.permanent.permanent.network.models

import android.os.Parcel
import android.os.Parcelable

class FileData private constructor() : Parcelable {
    var displayName: String? = null
    var fileURL: String? = null
    var downloadURL: String? = null
    var contentType: String? = null
    var fileName: String? = null

    constructor(recordVO: RecordVO) : this() {
        val fileVO: FileVO? = recordVO.FileVOs?.get(0)
        displayName = recordVO.displayName
        fileURL = fileVO?.fileURL
        downloadURL = fileVO?.downloadURL
        contentType = fileVO?.contentType
        fileName = recordVO.uploadFileName
    }

    constructor(parcel: Parcel) : this() {
        displayName = parcel.readString()
        fileURL = parcel.readString()
        downloadURL = parcel.readString()
        contentType = parcel.readString()
        fileName = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(displayName)
        parcel.writeString(fileURL)
        parcel.writeString(downloadURL)
        parcel.writeString(contentType)
        parcel.writeString(fileName)
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