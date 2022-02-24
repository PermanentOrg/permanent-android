package org.permanent.permanent.ui.publicWorkspace

import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemMilestoneBinding
import org.permanent.permanent.models.Milestone

class MilestoneViewHolder(
    private val binding: ItemMilestoneBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(milestone: Milestone) {
        binding.milestone = milestone
        binding.executePendingBindings()
    }
}