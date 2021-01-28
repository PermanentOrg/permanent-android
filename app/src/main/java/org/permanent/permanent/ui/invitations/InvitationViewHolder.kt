package org.permanent.permanent.ui.invitations

import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemInvitationBinding
import org.permanent.permanent.models.Invitation

class InvitationViewHolder(
    private val binding: ItemInvitationBinding, private val listener: InvitationListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(invitation: Invitation) {
        binding.invitation = invitation
        binding.executePendingBindings()
        binding.btnResend.setOnClickListener { listener.onResendClick(invitation) }
        binding.btnRevoke.setOnClickListener { listener.onRevokeClick(invitation) }
    }
}