package org.permanent.permanent.network

import org.permanent.permanent.models.Invitation

interface IPendingInvitesListener {
    fun onSuccess(invitations: List<Invitation>)
    fun onFailed(error: String?)
}
