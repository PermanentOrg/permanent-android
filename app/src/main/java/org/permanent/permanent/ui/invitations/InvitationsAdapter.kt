package org.permanent.permanent.ui.invitations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemInvitationBinding
import org.permanent.permanent.models.Invitation

class InvitationsAdapter(
    private val listener: InvitationListener
) : RecyclerView.Adapter<InvitationViewHolder>() {
    private var invitations: MutableList<Invitation> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvitationViewHolder {
        val binding = ItemInvitationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return InvitationViewHolder(binding, listener)
    }

    fun set(invitations: MutableList<Invitation>) {
        this.invitations = invitations
        notifyDataSetChanged()
    }

    override fun getItemCount() = invitations.size

    override fun onBindViewHolder(holder: InvitationViewHolder, position: Int) {
        holder.bind(invitations[position])
    }
}