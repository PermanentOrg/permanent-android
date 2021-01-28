package org.permanent.permanent.models

import org.permanent.permanent.network.models.InviteVO

class Invitation private constructor() {
    var inviteId: Int? = null
    var email: String? = null
    var fullName: String? = null
    var status: Status? = null

    constructor(inviteVO: InviteVO) : this() {
        inviteId = inviteVO.inviteId
        email = inviteVO.email
        fullName = inviteVO.fullName
        status = when (inviteVO.status) {
            Status.REVOKED.toBackendString() -> Status.REVOKED
            Status.ACCEPTED.toBackendString() -> Status.ACCEPTED
            else -> Status.PENDING
        }
    }

    enum class Status(private val backendString: String) {
        PENDING("status.invite.pending"),
        REVOKED("status.invite.revoked"),
        ACCEPTED("status.invite.accepted");

        fun toBackendString(): String = backendString
    }
}