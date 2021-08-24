package org.permanent.permanent.models

import android.os.Parcel
import android.os.Parcelable
import org.permanent.permanent.network.models.ArchiveVO

class Archive() : Parcelable {
    var id: Int = -1
    var fullName: String? = null
    var thumbURL500: String? = null
    var accessRole: AccessRole? = null
    var accessRoleText: String? = null

    constructor(parcel: Parcel) : this() {
        id = parcel.readInt()
        fullName = parcel.readString()
        thumbURL500 = parcel.readString()
        accessRole = parcel.readParcelable(AccessRole::class.java.classLoader)
        accessRoleText = parcel.readString()
    }

    constructor(archiveVO: ArchiveVO?) : this() {
        id = archiveVO?.archiveId ?: -1
        fullName = "The ${archiveVO?.fullName} Archive"
        thumbURL500 = archiveVO?.thumbURL500
        accessRole = when (archiveVO?.accessRole) {
            AccessRole.MANAGER.backendString -> AccessRole.MANAGER
            AccessRole.CURATOR.backendString -> AccessRole.CURATOR
            AccessRole.EDITOR.backendString -> AccessRole.EDITOR
            AccessRole.CONTRIBUTOR.backendString -> AccessRole.CONTRIBUTOR
            else -> AccessRole.VIEWER
        }
        accessRoleText = "Access: ${accessRole?.toTitleCase()}"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(fullName)
        parcel.writeString(thumbURL500)
        parcel.writeParcelable(accessRole, flags)
        parcel.writeString(accessRoleText)
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