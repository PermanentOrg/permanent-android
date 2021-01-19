package org.permanent.permanent.models

import android.os.Parcel
import android.os.Parcelable
import org.permanent.permanent.network.models.ArchiveVO

class Archive() : Parcelable {
    var fullName: String? = null
    var thumbURL200: String? = null
    var thumbURL500: String? = null
    var thumbURL1000: String? = null
    var thumbURL2000: String? = null

    constructor(parcel: Parcel) : this() {
        fullName = parcel.readString()
        thumbURL200 = parcel.readString()
        thumbURL500 = parcel.readString()
        thumbURL1000 = parcel.readString()
        thumbURL2000 = parcel.readString()
    }

    constructor(archiveVO: ArchiveVO?) : this() {
        fullName = archiveVO?.fullName
        thumbURL200 = archiveVO?.thumbURL200
        thumbURL500 = archiveVO?.thumbURL500
        thumbURL1000 = archiveVO?.thumbURL1000
        thumbURL2000 = archiveVO?.thumbURL2000
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(fullName)
        parcel.writeString(thumbURL200)
        parcel.writeString(thumbURL500)
        parcel.writeString(thumbURL1000)
        parcel.writeString(thumbURL2000)
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