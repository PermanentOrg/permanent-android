package org.permanent.permanent.ui.publicWorkspace

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemMilestoneBinding
import org.permanent.permanent.models.Milestone

class MilestonesAdapter : RecyclerView.Adapter<MilestoneViewHolder>() {
    private var milestones: MutableList<Milestone> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MilestoneViewHolder {
        val binding = ItemMilestoneBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MilestoneViewHolder(binding)
    }

    fun set(milestones: MutableList<Milestone>) {
        this.milestones = milestones
        notifyDataSetChanged()
    }

    override fun getItemCount() = milestones.size

    override fun onBindViewHolder(holder: MilestoneViewHolder, position: Int) {
        holder.bind(milestones[position])
    }
}