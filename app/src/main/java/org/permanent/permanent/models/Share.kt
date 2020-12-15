package org.permanent.permanent.models

import android.os.Parcel
import android.os.Parcelable

class Share() : Parcelable {
    var archive: Archive? = null

    constructor(parcel: Parcel) : this() {
        archive = parcel.readParcelable(Archive::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(archive, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Share> {
        override fun createFromParcel(parcel: Parcel): Share {
            return Share(parcel)
        }

        override fun newArray(size: Int): Array<Share?> {
            return arrayOfNulls(size)
        }
    }
}