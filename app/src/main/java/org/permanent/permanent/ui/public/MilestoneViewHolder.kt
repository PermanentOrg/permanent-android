package org.permanent.permanent.ui.public

import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemMilestoneBinding
import org.permanent.permanent.models.ProfileItem

class MilestoneViewHolder(
    private val binding: ItemMilestoneBinding, private val listener: ProfileItemListener?
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(profileItem: ProfileItem) {
        binding.profileItem = profileItem
        binding.executePendingBindings()
        binding.btnOptions.setOnClickListener { listener?.onOptionsClick(profileItem) }
        binding.layoutSwipeReveal.setLockDrag(profileItem.isForPublicProfileScreen)
        binding.btnDelete.setOnClickListener {
            listener?.onDeleteClick(profileItem)
            binding.layoutSwipeReveal.close(true)
        }
        binding.btnEdit.setOnClickListener {
            listener?.onEditClick(profileItem)
            binding.layoutSwipeReveal.close(true)
        }
    }
}