package org.permanent.permanent.models

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.network.models.ShareVO

class Share private constructor() : Parcelable {
    var id: Int? = null
    var folderLinkId: Int? = null
    var archiveId: Int? = null
    var archive: Archive? = null
    var accessRole: AccessRole? = null
    var status = MutableLiveData(Status.OK)

    constructor(parcel: Parcel) : this() {
        id = parcel.readValue(Int::class.java.classLoader) as? Int
        folderLinkId = parcel.readValue(Int::class.java.classLoader) as? Int
        archiveId = parcel.readValue(Int::class.java.classLoader) as? Int
        archive = parcel.readParcelable(Archive::class.java.classLoader)
        accessRole = parcel.readParcelable(AccessRole::class.java.classLoader)
        status.value = parcel.readParcelable(Status::class.java.classLoader)
    }

    constructor(shareVO: ShareVO) : this() {
        id = shareVO.shareId
        folderLinkId = shareVO.folder_linkId
        archiveId = shareVO.archiveId
        archive = Archive(shareVO.ArchiveVO)
        accessRole = when (shareVO.accessRole) {
            AccessRole.OWNER.backendString -> AccessRole.OWNER
            AccessRole.MANAGER.backendString -> AccessRole.MANAGER
            AccessRole.CURATOR.backendString -> AccessRole.CURATOR
            AccessRole.EDITOR.backendString -> AccessRole.EDITOR
            AccessRole.CONTRIBUTOR.backendString -> AccessRole.CONTRIBUTOR
            else -> AccessRole.VIEWER
        }
        status.value = when (shareVO.status) {
            Status.PENDING.toBackendString() -> Status.PENDING
            else -> Status.OK
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeValue(folderLinkId)
        parcel.writeValue(archiveId)
        parcel.writeParcelable(archive, flags)
        parcel.writeParcelable(accessRole, flags)
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