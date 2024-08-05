package org.permanent.permanent.models

import android.os.Parcel
import android.os.Parcelable
import org.permanent.permanent.network.models.ArchiveVO

class Archive() : Parcelable {
    var id: Int = -1
    var number: String? = null
    var thumbArchiveNr: String? = null
    var thumbStatus: ThumbStatus? = null
    var type: ArchiveType? = null
    var fullName: String? = null
    var thumbURL200: String? = null
    var accessRole: AccessRole? = null
    var accessRoleText: String? = null
    var status: Status? = null
    var isPublic: Int? = null
    var isPopular: Boolean = false

    constructor(parcel: Parcel) : this() {
        id = parcel.readValue(Int::class.java.classLoader) as Int
        number = parcel.readString()
        thumbArchiveNr = parcel.readString()
        thumbStatus = parcel.readParcelable(ThumbStatus::class.java.classLoader)
        type = parcel.readParcelable(ArchiveType::class.java.classLoader)
        fullName = parcel.readString()
        thumbURL200 = parcel.readString()
        accessRole = parcel.readParcelable(AccessRole::class.java.classLoader)
        accessRoleText = parcel.readString()
        status = parcel.readParcelable(Status::class.java.classLoader)
        isPublic = parcel.readValue(Int::class.java.classLoader) as? Int
        isPopular = parcel.readValue(Boolean::class.java.classLoader) as Boolean
    }

    constructor(archiveVO: ArchiveVO?) : this() {
        id = archiveVO?.archiveId ?: -1
        number = archiveVO?.archiveNbr
        thumbArchiveNr = archiveVO?.thumbArchiveNbr
        thumbStatus = ThumbStatus.createFromBackendString(archiveVO?.thumbStatus)
        type = when (archiveVO?.type) {
            ArchiveType.FAMILY.backendString -> ArchiveType.FAMILY
            ArchiveType.ORGANIZATION.backendString, ArchiveType.NONPROFIT.backendString -> ArchiveType.ORGANIZATION
            else -> ArchiveType.PERSON
        }
        fullName = "The ${archiveVO?.fullName} Archive"
        thumbURL200 = archiveVO?.thumbURL200
        accessRole = when (archiveVO?.accessRole) {
            AccessRole.OWNER.backendString -> AccessRole.OWNER
            AccessRole.MANAGER.backendString -> AccessRole.MANAGER
            AccessRole.CURATOR.backendString -> AccessRole.CURATOR
            AccessRole.EDITOR.backendString -> AccessRole.EDITOR
            AccessRole.CONTRIBUTOR.backendString -> AccessRole.CONTRIBUTOR
            else -> AccessRole.VIEWER
        }
        accessRoleText = "Access: ${accessRole?.toTitleCase()}"
        status = when (archiveVO?.status) {
            Status.PENDING.toBackendString() -> Status.PENDING
            else -> Status.OK
        }
        isPublic = archiveVO?.public
    }

    constructor(archiveId: Int) : this() {
        id = archiveId
    }

    constructor(
        id: Int,
        number: String?,
        type: ArchiveType,
        fullName: String?,
        thumbURL: String?,
        accessRole: AccessRole
    ) : this() {
        this.id = id
        this.number = number
        this.type = type
        this.fullName = fullName
        this.thumbURL200 = thumbURL
        this.accessRole = accessRole
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(number)
        parcel.writeString(thumbArchiveNr)
        parcel.writeParcelable(thumbStatus, flags)
        parcel.writeParcelable(type, flags)
        parcel.writeString(fullName)
        parcel.writeString(thumbURL200)
        parcel.writeParcelable(accessRole, flags)
        parcel.writeString(accessRoleText)
        parcel.writeParcelable(status, flags)
        parcel.writeValue(isPublic)
        parcel.writeValue(isPopular)
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