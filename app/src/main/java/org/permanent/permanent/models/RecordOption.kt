package org.permanent.permanent.models

import android.os.Parcel
import android.os.Parcelable

enum class RecordOption : Parcelable {
    DOWNLOAD,
    COPY,
    MOVE,
    PUBLISH,
    COPY_LINK,
    DELETE,
    RENAME,
    SHARE_VIA_PERMANENT,
    SHARE_TO_ANOTHER_APP,
    UNSHARE;

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(ordinal)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RecordOption> {
        override fun createFromParcel(parcel: Parcel): RecordOption {
            return values()[parcel.readInt()]
        }

        override fun newArray(size: Int): Array<RecordOption?> {
            return arrayOfNulls(size)
        }
    }
}
