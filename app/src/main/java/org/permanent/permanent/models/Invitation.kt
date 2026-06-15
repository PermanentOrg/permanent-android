package org.permanent.permanent.models

import org.permanent.permanent.network.models.InviteVO
import org.permanent.permanent.network.models.PendingShareDTO

class Invitation private constructor() {
    var inviteId: Int? = null
    var email: String? = null
    var fullName: String? = null
    var status: Status? = null
    var accessRole: AccessRole? = null
    var byArchiveId: Int? = null
    var timesSent: Int? = null
    var expiresDT: String? = null
    var createdDT: String? = null
    var updatedDT: String? = null

    constructor(inviteVO: InviteVO) : this() {
        inviteId = inviteVO.inviteId
        email = inviteVO.email
        fullName = inviteVO.fullName
        status = when (inviteVO.status) {
            Status.REVOKED.toBackendString() -> Status.REVOKED
            Status.ACCEPTED.toBackendString() -> Status.ACCEPTED
            else -> Status.PENDING
        }
        accessRole = inviteVO.accessRole?.let { AccessRole.fromBackendValue(it) }
        byArchiveId = inviteVO.byArchiveId
        timesSent = inviteVO.timesSent
        expiresDT = inviteVO.expiresDT
        createdDT = inviteVO.createdDT
        updatedDT = inviteVO.updatedDT
    }

    // Maps a pending invitation from the v2 GET /api/v2/records/{recordId} response.
    constructor(pendingShare: PendingShareDTO) : this() {
        // CAVEAT: v2 stringifies the id; resend/revoke key off inviteId: Int. Whether this
        // id equals the inviteId returned by invite/share is unverified — see plan section 8.
        inviteId = pendingShare.id?.toIntOrNull()
        email = pendingShare.email
        fullName = pendingShare.name
        accessRole = pendingShare.accessRole?.let { AccessRole.fromBackendValue(it) }
        status = Status.PENDING
    }

    enum class Status(private val backendString: String) {
        PENDING("status.invite.pending"),
        REVOKED("status.invite.revoked"),
        ACCEPTED("status.invite.accepted");

        fun toBackendString(): String = backendString
    }
}