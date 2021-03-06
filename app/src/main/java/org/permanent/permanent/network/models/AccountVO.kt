package org.permanent.permanent.network.models

import org.permanent.permanent.models.Account

class AccountVO() {
    var accountId: Int? = null
    var primaryEmail: String? = null
    var rememberMe: Boolean? = null
    var agreed: Boolean? = null
    var optIn: Boolean? = null
    var fullName: String? = null
    var primaryPhone: String? = null
    var address: String? = null
    var country: String? = null
    var city: String? = null
    var state: String? = null
    var zip: String? = null
    var accessRole: String? = null
    var status: String? = null
    var spaceTotal: Double? = null
    var spaceLeft: Double? = null

    constructor(account: Account) : this() {
        accountId = account.id
        fullName = account.fullName
        primaryEmail = account.primaryEmail
        primaryPhone = account.phone
        address = account.address
        country = account.country
        city = account.city
        state = account.state
        zip = account.zipCode
        accessRole = account.accessRole?.backendString
        status = account.status?.toBackendString()
        spaceTotal = account.spaceTotal
    }
}