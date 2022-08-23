package org.permanent.permanent

import android.os.Parcel
import android.os.Parcelable
import java.util.*

enum class ArchivePermission : Parcelable {
    READ, CREATE, EDIT, DELETE, MOVE, PUBLISH, SHARE, ARCHIVE_SHARE, OWNERSHIP;

    fun toTitleCase(): String = this.name.lowercase(Locale.getDefault())
        .replaceFirstChar { it.titlecase(Locale.getDefault()) }

    fun toLowerCase(): String = this.name.lowercase(Locale.getDefault())

    fun toUIString(): String {
        return when {
            this == READ -> "view"
            this == ARCHIVE_SHARE -> SHARE.name.lowercase(Locale.getDefault())
            else -> this.name.lowercase(Locale.getDefault())
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(ordinal)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ArchivePermission> {
        override fun createFromParcel(parcel: Parcel): ArchivePermission {
            return values()[parcel.readInt()]
        }

        override fun newArray(size: Int): Array<ArchivePermission?> {
            return arrayOfNulls(size)
        }
    }
}