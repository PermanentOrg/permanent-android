package org.permanent.permanent.models

import org.permanent.permanent.network.models.AccountVO

class Account private constructor() {

    var id: Int? = null
    var fullName: String? = null
    var primaryEmail: String? = null
    var accessRole: AccessRole? = null
    var status: Status? = null

    constructor(accountVO: AccountVO?) : this() {
        id = accountVO?.accountId
        fullName = accountVO?.fullName
        primaryEmail = accountVO?.primaryEmail
        val accountVORole = accountVO?.accessRole
        val accountVOStatus = accountVO?.status
        accessRole = when (accountVORole) {
            AccessRole.MANAGER.toBackendString() -> AccessRole.MANAGER
            AccessRole.CURATOR.toBackendString() -> AccessRole.CURATOR
            AccessRole.EDITOR.toBackendString() -> AccessRole.EDITOR
            AccessRole.CONTRIBUTOR.toBackendString() -> AccessRole.CONTRIBUTOR
            else -> AccessRole.VIEWER
        }
        status = when (accountVOStatus) {
            Status.PENDING.toBackendString() -> Status.PENDING
            else -> Status.OK
        }
    }
}