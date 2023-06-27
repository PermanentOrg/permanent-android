package org.permanent.permanent.ui

import android.os.Parcel
import android.os.Parcelable

enum class Workspace : Parcelable {
    PRIVATE_FILES,
    PUBLIC_FILES,
    PUBLIC_ARCHIVES,
    SHARES,
    SHARED_BY_ME,
    SHARED_WITH_ME;

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(ordinal)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Workspace> {
        override fun createFromParcel(parcel: Parcel): Workspace {
            return values()[parcel.readInt()]
        }

        override fun newArray(size: Int): Array<Workspace?> {
            return arrayOfNulls(size)
        }
    }
}
