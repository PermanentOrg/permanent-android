package org.permanent.permanent.ui.publicWorkspace

import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_list_record.view.*
import kotlinx.android.synthetic.main.item_online_presence_underlay.view.*
import org.permanent.permanent.databinding.ItemOnlinePresenceBinding
import org.permanent.permanent.models.ProfileItem

class OnlinePresenceListViewHolder (
    private val binding: ItemOnlinePresenceBinding,
    private val listener: OnlinePresenceListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(profileItem: ProfileItem) {
        binding.profileItem = profileItem
        binding.executePendingBindings()
        binding.btnOptions.setOnClickListener { listener.onOptionsClick(profileItem) }
        binding.layoutSwipeReveal.layoutUnderlay.getChildAt(0).btnDeleteOnlinePresence
            .setOnClickListener {
                listener.onDeleteClick(profileItem)
                binding.layoutSwipeReveal.close(true)
            }
        binding.layoutSwipeReveal.layoutUnderlay.getChildAt(0).btnEditOnlinePresence
            .setOnClickListener {
                listener.onEditClick(profileItem)
                binding.layoutSwipeReveal.close(true)
            }
    }
}