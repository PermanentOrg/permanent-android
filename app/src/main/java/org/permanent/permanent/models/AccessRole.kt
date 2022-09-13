package org.permanent.permanent.models

import android.os.Parcel
import android.os.Parcelable
import java.util.*

enum class AccessRole(val backendString: String) : Parcelable {
    OWNER("access.role.owner") {
        override fun inferiors(): List<AccessRole> =
            listOf(MANAGER, CURATOR, EDITOR, CONTRIBUTOR, VIEWER)
    },
    MANAGER("access.role.manager") {
        override fun inferiors(): List<AccessRole> =
            listOf(CURATOR, EDITOR, CONTRIBUTOR, VIEWER)
    },
    CURATOR("access.role.curator") {
        override fun inferiors(): List<AccessRole> =
            listOf(EDITOR, CONTRIBUTOR, VIEWER)
    },
    EDITOR("access.role.editor") {
        override fun inferiors(): List<AccessRole> =
            listOf(CONTRIBUTOR, VIEWER)
    },
    CONTRIBUTOR("access.role.contributor") {
        override fun inferiors(): List<AccessRole> =
            listOf(VIEWER)
    },
    VIEWER("access.role.viewer") {
        override fun inferiors(): List<AccessRole> =
            listOf()
    };

    abstract fun inferiors(): List<AccessRole>

    fun toTitleCase(): String = this.name.lowercase(Locale.getDefault())
        .replaceFirstChar { it.titlecase(Locale.getDefault()) }

    fun toLowerCase(): String = this.name.lowercase(Locale.getDefault())

    fun getInferior(otherAccessRole: AccessRole): AccessRole {
        return if (otherAccessRole in inferiors()) otherAccessRole else this
    }

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

        fun createFromBackendString(accessRoleString: String?): AccessRole {
            return when (accessRoleString) {
                OWNER.backendString -> OWNER
                MANAGER.backendString -> MANAGER
                CURATOR.backendString -> CURATOR
                EDITOR.backendString -> EDITOR
                CONTRIBUTOR.backendString -> CONTRIBUTOR
                else -> VIEWER
            }
        }

        override fun newArray(size: Int): Array<AccessRole?> {
            return arrayOfNulls(size)
        }
    }
}