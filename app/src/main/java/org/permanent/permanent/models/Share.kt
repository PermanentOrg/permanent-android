package org.permanent.permanent.models

import android.os.Parcel
import android.os.Parcelable
import org.permanent.permanent.network.models.ShareVO

class Share private constructor() : Parcelable {
    var archive: Archive? = null
    var status: Status? = null

    constructor(parcel: Parcel) : this() {
        archive = parcel.readParcelable(Archive::class.java.classLoader)
        status = parcel.readParcelable(Status::class.java.classLoader)
    }

    constructor(shareVO: ShareVO) : this() {
        archive = Archive(shareVO.ArchiveVO)
        status = when (shareVO.status) {
            Status.PENDING.toBackendString() -> Status.PENDING
            else -> Status.OK
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(archive, flags)
        parcel.writeParcelable(status, flags)
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