package org.permanent.permanent.repositories

import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.ui.invitations.UpdateType

interface IInvitationRepository {

    fun getInvitations(listener: IDataListener)

    fun sendInvitation(name: String, email: String, listener: IResponseListener)

    fun updateInvitation(inviteId: Int, type: UpdateType, listener: IResponseListener)
}