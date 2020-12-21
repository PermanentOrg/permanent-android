package org.permanent.permanent.ui.shares

import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemShareBinding
import org.permanent.permanent.models.ShareItem

class ShareViewHolder (
    private val binding: ItemShareBinding,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(shareItem: ShareItem) {
        binding.shareItem = shareItem
        binding.executePendingBindings()
    }
}