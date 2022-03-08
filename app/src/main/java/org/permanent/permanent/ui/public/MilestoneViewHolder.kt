package org.permanent.permanent.ui.public

import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_list_record.view.*
import kotlinx.android.synthetic.main.item_online_presence_underlay.view.*
import org.permanent.permanent.databinding.ItemMilestoneBinding
import org.permanent.permanent.models.ProfileItem

class MilestoneViewHolder(
    private val binding: ItemMilestoneBinding,
    private val listener: ProfileItemListener?
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(profileItem: ProfileItem) {
        binding.profileItem = profileItem
        binding.executePendingBindings()
        binding.btnOptions.setOnClickListener { listener?.onOptionsClick(profileItem) }
        binding.layoutSwipeReveal.setLockDrag(profileItem.isForPublicProfileScreen)
        binding.layoutSwipeReveal.layoutUnderlay.getChildAt(0).btnDeleteOnlinePresence
            .setOnClickListener {
                listener?.onDeleteClick(profileItem)
                binding.layoutSwipeReveal.close(true)
            }
        binding.layoutSwipeReveal.layoutUnderlay.getChildAt(0).btnEditOnlinePresence
            .setOnClickListener {
                listener?.onEditClick(profileItem)
                binding.layoutSwipeReveal.close(true)
            }
    }
}