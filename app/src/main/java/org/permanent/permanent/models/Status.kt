package org.permanent.permanent.models

import android.os.Parcel
import android.os.Parcelable

enum class Status(private val backendString: String) : Parcelable {
    OK("status.generic.ok"),
    PENDING("status.generic.pending");

    fun toBackendString(): String = backendString

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(ordinal)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Status> {
        override fun createFromParcel(parcel: Parcel): Status {
            return values()[parcel.readInt()]
        }

        override fun newArray(size: Int): Array<Status?> {
            return arrayOfNulls(size)
        }
    }
}