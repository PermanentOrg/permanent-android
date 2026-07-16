package org.permanent.permanent.repositories

import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IInviteListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.ui.invitations.UpdateType

interface IInvitationRepository {

    fun getInvitations(listener: IDataListener)

    fun sendInvitation(name: String, email: String, listener: IResponseListener)

    fun updateInvitation(inviteId: Int, type: UpdateType, listener: IResponseListener)

    fun shareInvitation(
        email: String,
        fullName: String,
        accessRole: AccessRole,
        recordId: Int,
        folderLinkId: Int,
        byArchiveId: Int,
        listener: IInviteListener
    )

    fun updateInvitationReturningInvite(inviteId: Int, type: UpdateType, listener: IInviteListener)
}