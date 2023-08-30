package org.permanent.permanent.network.models

import android.os.Parcel
import android.os.Parcelable


class ArchiveSteward() : Parcelable {
    class StewardDetails {
        var name: String? = null
        var email: String? = null
    }

    class Trigger(type: String) {
        var type: String? = type
    }

    var directiveId: String? = null
    var archiveId: String? = null
    var type: String? = null
    var trigger: Trigger? = null
    var createdDT: String? = null
    var updatedDT: String? = null
    var stewardAccountId: String? = null
    var stewardEmail: String? = null
    var note: String? = null
    var executionDT: String? = null
    var steward: StewardDetails? = null

    constructor(parcel: Parcel) : this() {
        directiveId = parcel.readString()
        archiveId = parcel.readString()
        type = parcel.readString()
        createdDT = parcel.readString()
        updatedDT = parcel.readString()
        stewardAccountId = parcel.readString()
        stewardEmail = parcel.readString()
        note = parcel.readString()
        executionDT = parcel.readString()
    }

    constructor(archiveId: Int?, email: String, note: String?) : this() {
        this.archiveId = archiveId?.toString()
        this.stewardEmail = email
        this.type = "transfer"
        this.trigger = Trigger("admin")
        this.note = note
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(directiveId)
        parcel.writeString(archiveId)
        parcel.writeString(type)
        parcel.writeString(createdDT)
        parcel.writeString(updatedDT)
        parcel.writeString(stewardAccountId)
        parcel.writeString(stewardEmail)
        parcel.writeString(note)
        parcel.writeString(executionDT)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ArchiveSteward> {
        override fun createFromParcel(parcel: Parcel): ArchiveSteward {
            return ArchiveSteward(parcel)
        }

        override fun newArray(size: Int): Array<ArchiveSteward?> {
            return arrayOfNulls(size)
        }
    }
}