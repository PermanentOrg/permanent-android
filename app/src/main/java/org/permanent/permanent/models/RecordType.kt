package org.permanent.permanent.models

import android.os.Parcel
import android.os.Parcelable

enum class RecordType : Parcelable {
    FILE, FOLDER;

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(ordinal)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RecordType> {
        override fun createFromParcel(parcel: Parcel): RecordType {
            return values()[parcel.readInt()]
        }

        override fun newArray(size: Int): Array<RecordType?> {
            return arrayOfNulls(size)
        }
    }
}