package org.permanent.permanent.models

import android.os.Parcel
import android.os.Parcelable
import java.util.*

enum class AccessRole(val backendString: String) : Parcelable {
    OWNER("access.role.owner"),
    MANAGER("access.role.manager"),
    CURATOR("access.role.curator"),
    EDITOR("access.role.editor"),
    CONTRIBUTOR("access.role.contributor"),
    VIEWER("access.role.viewer");

    fun toTitleCase(): String = this.name.lowercase(Locale.getDefault())
        .replaceFirstChar { it.titlecase(Locale.getDefault()) }

    fun toLowerCase(): String = this.name.lowercase(Locale.getDefault())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(ordinal)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AccessRole> {
        override fun createFromParcel(parcel: Parcel): AccessRole {
            return values()[parcel.readInt()]
        }

        override fun newArray(size: Int): Array<AccessRole?> {
            return arrayOfNulls(size)
        }
    }
}