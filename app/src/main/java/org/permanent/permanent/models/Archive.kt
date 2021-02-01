package org.permanent.permanent.models

import android.os.Parcel
import android.os.Parcelable
import org.permanent.permanent.network.models.ArchiveVO

class Archive() : Parcelable {
    var fullName: String? = null
    var thumbURL500: String? = null

    constructor(parcel: Parcel) : this() {
        fullName = parcel.readString()
        thumbURL500 = parcel.readString()
    }

    constructor(archiveVO: ArchiveVO?) : this() {
        fullName = archiveVO?.fullName
        thumbURL500 = archiveVO?.thumbURL500
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(fullName)
        parcel.writeString(thumbURL500)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Archive> {
        override fun createFromParcel(parcel: Parcel): Archive {
            return Archive(parcel)
        }

        override fun newArray(size: Int): Array<Archive?> {
            return arrayOfNulls(size)
        }
    }
}