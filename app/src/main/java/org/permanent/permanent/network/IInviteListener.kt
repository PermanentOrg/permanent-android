package org.permanent.permanent.network

import org.permanent.permanent.models.Invitation

interface IInviteListener {
    fun onSuccess(invitation: Invitation?)
    fun onFailed(error: String?)
}
