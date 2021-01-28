package org.permanent.permanent.ui.invitations

import org.permanent.permanent.models.Invitation

interface InvitationListener {

    fun onResendClick(invitation: Invitation)

    fun onRevokeClick(invitation: Invitation)
}
