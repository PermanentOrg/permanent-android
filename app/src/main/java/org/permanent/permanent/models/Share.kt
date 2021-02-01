package org.permanent.permanent.models

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.network.models.ShareVO

class Share private constructor() : Parcelable {
    var shareId: Int? = null
    var folderLinkId: Int? = null
    var archiveId: Int? = null
    var archive: Archive? = null
    var accessRole: String? = null
    var status = MutableLiveData(Status.OK)

    constructor(parcel: Parcel) : this() {
        shareId = parcel.readValue(Int::class.java.classLoader) as? Int
        folderLinkId = parcel.readValue(Int::class.java.classLoader) as? Int
        archiveId = parcel.readValue(Int::class.java.classLoader) as? Int
        archive = parcel.readParcelable(Archive::class.java.classLoader)
        accessRole = parcel.readString()
        status.value = parcel.readParcelable(Status::class.java.classLoader)
    }

    constructor(shareVO: ShareVO) : this() {
        shareId = shareVO.shareId
        folderLinkId = shareVO.folder_linkId
        archiveId = shareVO.archiveId
        archive = Archive(shareVO.ArchiveVO)
        accessRole = shareVO.accessRole
        status.value = when (shareVO.status) {
            Status.PENDING.toBackendString() -> Status.PENDING
            else -> Status.OK
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(shareId)
        parcel.writeValue(folderLinkId)
        parcel.writeValue(archiveId)
        parcel.writeParcelable(archive, flags)
        parcel.writeString(accessRole)
        parcel.writeParcelable(status.value, flags)
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