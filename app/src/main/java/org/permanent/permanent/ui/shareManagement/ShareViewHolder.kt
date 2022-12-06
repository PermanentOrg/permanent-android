package org.permanent.permanent.ui.shareManagement

import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemShareBinding
import org.permanent.permanent.models.Share

class ShareViewHolder(
    private val binding: ItemShareBinding, val listener: ShareListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(share: Share) {
        binding.share = share
        binding.executePendingBindings()
        binding.btnEdit.setOnClickListener { listener.onEditClick(share) }
        binding.btnApprove.setOnClickListener { listener.onApproveClick(share) }
        binding.btnDeny.setOnClickListener { listener.onDenyClick(share) }
    }
}