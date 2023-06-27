package org.permanent.permanent.ui.public

import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemOnlinePresenceBinding
import org.permanent.permanent.models.ProfileItem

class OnlinePresenceListViewHolder(
    private val binding: ItemOnlinePresenceBinding,
    private val listener: ProfileItemListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(profileItem: ProfileItem) {
        binding.profileItem = profileItem
        binding.executePendingBindings()
        binding.btnOptions.setOnClickListener { listener.onOptionsClick(profileItem) }
        binding.btnDeleteOnlinePresence.setOnClickListener {
            listener.onDeleteClick(profileItem)
            binding.layoutSwipeReveal.close(true)
        }
        binding.btnEditOnlinePresence.setOnClickListener {
            listener.onEditClick(profileItem)
            binding.layoutSwipeReveal.close(true)
        }
    }
}