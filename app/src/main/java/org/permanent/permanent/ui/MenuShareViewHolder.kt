package org.permanent.permanent.ui

import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemMenuShareBinding
import org.permanent.permanent.models.Share

class MenuShareViewHolder(private val binding: ItemMenuShareBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(share: Share) {
        binding.share = share
        binding.executePendingBindings()
    }
}