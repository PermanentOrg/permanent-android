package org.permanent.permanent.ui.public

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemMilestoneBinding
import org.permanent.permanent.models.ProfileItem

class MilestonesAdapter(
    private val milestoneListener: ProfileItemListener?
) : RecyclerView.Adapter<MilestoneViewHolder>() {
    private var profileItems: MutableList<ProfileItem> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MilestoneViewHolder {
        val binding = ItemMilestoneBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MilestoneViewHolder(binding, milestoneListener)
    }

    fun set(profileItems: MutableList<ProfileItem>) {
        this.profileItems = profileItems
        notifyDataSetChanged()
    }

    override fun getItemCount() = profileItems.size

    override fun onBindViewHolder(holder: MilestoneViewHolder, position: Int) {
        holder.bind(profileItems[position])
    }
}
