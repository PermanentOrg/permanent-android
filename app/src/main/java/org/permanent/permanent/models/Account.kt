package org.permanent.permanent.models

import org.permanent.permanent.network.models.AccountVO

class Account private constructor() {

    var id: Int? = null
    var fullName: String? = null
    var primaryEmail: String? = null
    var accessRole: AccessRole? = null

    constructor(accountVO: AccountVO?) : this() {
        id = accountVO?.accountId
        fullName = accountVO?.fullName
        primaryEmail = accountVO?.primaryEmail
        val accountVORole = accountVO?.accessRole
        accessRole = when {
            accountVORole?.contains(AccessRole.MANAGER.toLowerCase()) == true -> AccessRole.MANAGER
            accountVORole?.contains(AccessRole.CURATOR.toLowerCase()) == true -> AccessRole.CURATOR
            accountVORole?.contains(AccessRole.EDITOR.toLowerCase()) == true -> AccessRole.EDITOR
            accountVORole?.contains(AccessRole.CONTRIBUTOR.toLowerCase()) == true -> AccessRole.CONTRIBUTOR
            else -> AccessRole.VIEWER
        }
    }
}