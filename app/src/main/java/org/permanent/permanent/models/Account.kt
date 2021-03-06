package org.permanent.permanent.models

import org.permanent.permanent.network.models.AccountVO

class Account(var id: Int? = null, var primaryEmail: String? = null) {
    var fullName: String? = null
    var phone: String? = null
    var address: String? = null
    var country: String? = null
    var city: String? = null
    var state: String? = null
    var zipCode: String? = null
    var accessRole: AccessRole? = null
    var status: Status? = null
    var spaceTotal: Double? = null
    var spaceLeft: Double? = null

    constructor(accountVO: AccountVO?) : this() {
        id = accountVO?.accountId
        fullName = accountVO?.fullName
        primaryEmail = accountVO?.primaryEmail
        phone = accountVO?.primaryPhone
        address = accountVO?.address
        country = accountVO?.country
        city = accountVO?.city
        state = accountVO?.state
        zipCode = accountVO?.zip
        accessRole = when (accountVO?.accessRole) {
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
    }
}