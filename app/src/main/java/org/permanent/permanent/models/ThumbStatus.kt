package org.permanent.permanent.models

import android.os.Parcel
import android.os.Parcelable

enum class ThumbStatus(private val backendString: String?) : Parcelable {
    NULL(null),
    OK("status.generic.ok"),
    RECORD_NEEDS_THUMB("status.record.needs_thumb"),
    FOLDER_EMPTY("status.folder.empty");

    fun toBackendString(): String? = backendString

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(ordinal)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Status> {
        override fun createFromParcel(parcel: Parcel): Status {
            return Status.values()[parcel.readInt()]
        }

        fun createFromBackendString(backendString: String?): ThumbStatus {
            return when (backendString) {
                OK.backendString -> OK
                RECORD_NEEDS_THUMB.backendString -> RECORD_NEEDS_THUMB
                FOLDER_EMPTY.backendString -> FOLDER_EMPTY
                else -> NULL
            }
        }

        override fun newArray(size: Int): Array<Status?> {
            return arrayOfNulls(size)
        }
    }
}