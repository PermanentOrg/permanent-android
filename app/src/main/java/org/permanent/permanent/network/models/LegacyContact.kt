package org.permanent.permanent.network.models

import android.os.Parcel
import android.os.Parcelable

class LegacyContact() : Parcelable {
    var legacyContactId: String? = null
    var accountId: String? = null
    var name: String? = null
    var email: String? = null
    var createdDt: String? = null
    var updatedDt: String? = null

    constructor(parcel: Parcel) : this() {
        legacyContactId = parcel.readString()
        accountId = parcel.readString()
        name = parcel.readString()
        email = parcel.readString()
        createdDt = parcel.readString()
        updatedDt = parcel.readString()
    }

    constructor(email: String, name: String) : this() {
        this.name = name
        this.email = email
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(legacyContactId)
        parcel.writeString(accountId)
        parcel.writeString(name)
        parcel.writeString(email)
        parcel.writeString(createdDt)
        parcel.writeString(updatedDt)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LegacyContact> {
        override fun createFromParcel(parcel: Parcel): LegacyContact {
            return LegacyContact(parcel)
        }

        override fun newArray(size: Int): Array<LegacyContact?> {
            return arrayOfNulls(size)
        }
    }
}