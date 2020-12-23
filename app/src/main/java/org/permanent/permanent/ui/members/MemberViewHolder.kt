package org.permanent.permanent.ui.members

import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemMemberBinding
import org.permanent.permanent.models.Account

class MemberViewHolder (
    private val binding: ItemMemberBinding,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(member: Account) {
        binding.member = member
        binding.executePendingBindings()
    }
}