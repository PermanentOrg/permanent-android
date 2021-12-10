package org.permanent.permanent.network.models

import org.permanent.permanent.models.Account

class AccountVO() {
    var accountId: Int? = null
    var defaultArchiveId: Int? = null
    var primaryEmail: String? = null
    var rememberMe: Boolean? = null
    var agreed: Boolean? = null
    var optIn: Boolean? = null
    var fullName: String? = null
    var primaryPhone: String? = null
    var address: String? = null
    var address2: String? = null
    var country: String? = null
    var city: String? = null
    var state: String? = null
    var zip: String? = null
    var accessRole: String? = null
    var status: String? = null
    var spaceTotal: Long? = null
    var spaceLeft: Long? = null

    constructor(account: Account) : this() {
        accountId = account.id
        defaultArchiveId = account.defaultArchiveId
        fullName = account.fullName
        primaryEmail = account.primaryEmail
        primaryPhone = account.phone
        address = account.address
        address2 = account.addressTwo
        country = account.country
        city = account.city
        state = account.state
        zip = account.zipCode
        accessRole = account.accessRole?.backendString
        status = account.status?.toBackendString()
        spaceTotal = account.spaceTotal
    }
}