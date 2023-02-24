package org.permanent.permanent.models

import android.os.Parcel
import android.os.Parcelable
import org.permanent.permanent.network.models.AccountVO

class Account(var id: Int? = null, var primaryEmail: String? = null) : Parcelable {
    var defaultArchiveId: Int? = null
    var fullName: String? = null
    var phone: String? = null
    var address: String? = null
    var addressTwo: String? = null
    var country: String? = null
    var city: String? = null
    var state: String? = null
    var zipCode: String? = null
    var accessRole: AccessRole? = null
    var status: Status? = null
    var spaceTotal: Long? = null
    var spaceLeft: Long? = null
    var token: String? = null

    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString()
    ) {
        defaultArchiveId = parcel.readValue(Int::class.java.classLoader) as? Int
        fullName = parcel.readString()
        phone = parcel.readString()
        address = parcel.readString()
        addressTwo = parcel.readString()
        country = parcel.readString()
        city = parcel.readString()
        state = parcel.readString()
        zipCode = parcel.readString()
        accessRole = parcel.readParcelable(AccessRole::class.java.classLoader)
        status = parcel.readParcelable(Status::class.java.classLoader)
        spaceTotal = parcel.readValue(Long::class.java.classLoader) as? Long
        spaceLeft = parcel.readValue(Long::class.java.classLoader) as? Long
        token = parcel.readString()
    }

    constructor(accountVO: AccountVO?) : this() {
        id = accountVO?.accountId
        defaultArchiveId = accountVO?.defaultArchiveId
        fullName = accountVO?.fullName
        primaryEmail = accountVO?.primaryEmail
        phone = accountVO?.primaryPhone
        address = accountVO?.address
        addressTwo = accountVO?.address2
        country = accountVO?.country
        city = accountVO?.city
        state = accountVO?.state
        zipCode = accountVO?.zip
        accessRole = when (accountVO?.accessRole) {
            AccessRole.OWNER.backendString -> AccessRole.OWNER
            AccessRole.MANAGER.backendString -> AccessRole.MANAGER
            AccessRole.CURATOR.backendString -> AccessRole.CURATOR
            AccessRole.EDITOR.backendString -> AccessRole.EDITOR
            AccessRole.CONTRIBUTOR.backendString -> AccessRole.CONTRIBUTOR
            else -> AccessRole.VIEWER
        }
        status = when (accountVO?.status) {
            Status.PENDING.toBackendString() -> Status.PENDING
            else -> Status.OK
        }
        spaceTotal = accountVO?.spaceTotal
        spaceLeft = accountVO?.spaceLeft
        token = accountVO?.token
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(primaryEmail)
        parcel.writeValue(defaultArchiveId)
        parcel.writeString(fullName)
        parcel.writeString(phone)
        parcel.writeString(address)
        parcel.writeString(addressTwo)
        parcel.writeString(country)
        parcel.writeString(city)
        parcel.writeString(state)
        parcel.writeString(zipCode)
        parcel.writeParcelable(accessRole, flags)
        parcel.writeParcelable(status, flags)
        parcel.writeValue(spaceTotal)
        parcel.writeValue(spaceLeft)
        parcel.writeString(token)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Account> {
        override fun createFromParcel(parcel: Parcel): Account {
            return Account(parcel)
        }

        override fun newArray(size: Int): Array<Account?> {
            return arrayOfNulls(size)
        }
    }
}