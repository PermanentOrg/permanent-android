package org.permanent.permanent.models

import android.os.Parcel
import android.os.Parcelable
import java.util.*

enum class ArchiveType(val backendString: String) : Parcelable {
    PERSON("type.archive.person"),
    FAMILY("type.archive.family"),
    ORGANIZATION("type.archive.organization"),
    NONPROFIT("type.archive.nonprofit");

    fun toTitleCase(): String = this.name.lowercase(Locale.getDefault())
        .replaceFirstChar { it.titlecase(Locale.getDefault()) }

    fun toLowerCase(): String = this.name.lowercase(Locale.getDefault())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(ordinal)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ArchiveType> {
        override fun createFromParcel(parcel: Parcel): ArchiveType {
            return values()[parcel.readInt()]
        }

        override fun newArray(size: Int): Array<ArchiveType?> {
            return arrayOfNulls(size)
        }
    }
}