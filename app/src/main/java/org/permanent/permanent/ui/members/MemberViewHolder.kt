package org.permanent.permanent.ui.members

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.ArchivePermissionsManager
import org.permanent.permanent.databinding.ItemMemberBinding
import org.permanent.permanent.models.Account

class MemberViewHolder(
    private val binding: ItemMemberBinding,
    private val memberListener: MemberListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(member: Account) {
        binding.member = member
        binding.executePendingBindings()
        binding.btnOptions.visibility =
            if (ArchivePermissionsManager.instance.isArchiveShareAvailable())
                View.VISIBLE else View.INVISIBLE
        binding.btnOptions.setOnClickListener { memberListener.onMemberOptionsClick(member) }
    }
}