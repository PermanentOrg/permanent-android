package org.permanent.permanent.ui.public

import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_list_record.view.*
import kotlinx.android.synthetic.main.item_online_presence_underlay.view.*
import org.permanent.permanent.databinding.ItemMilestoneBinding
import org.permanent.permanent.models.Milestone

class MilestoneViewHolder(
    private val binding: ItemMilestoneBinding,
    private val listener: MilestoneListener?
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(milestone: Milestone) {
        binding.milestone = milestone
        binding.executePendingBindings()
        binding.btnOptions.setOnClickListener { listener?.onOptionsClick(milestone) }
        binding.layoutSwipeReveal.setLockDrag(milestone.isForPublicProfileScreen)
        binding.layoutSwipeReveal.layoutUnderlay.getChildAt(0).btnDeleteOnlinePresence
            .setOnClickListener {
                listener?.onDeleteClick(milestone)
                binding.layoutSwipeReveal.close(true)
            }
        binding.layoutSwipeReveal.layoutUnderlay.getChildAt(0).btnEditOnlinePresence
            .setOnClickListener {
                listener?.onEditClick(milestone)
                binding.layoutSwipeReveal.close(true)
            }
    }
}