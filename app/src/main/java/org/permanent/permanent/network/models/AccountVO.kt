package org.permanent.permanent.network.models

class AccountVO {
    var accountId: String? = null
    var primaryEmail: String? = null
    var rememberMe: Boolean? = false
    var agreed: Boolean? = false
    var optIn: Boolean? = false
    var fullName: String? = null
    var inviteCode: String? = null
    var primaryPhone: String? = null
}