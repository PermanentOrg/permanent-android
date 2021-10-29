package org.permanent.permanent.ui.myFiles.linkshare

import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemShareBinding
import org.permanent.permanent.models.Share

class ShareViewHolder(
    private val binding: ItemShareBinding, val listener: ShareListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(share: Share, lifecycleOwner: LifecycleOwner) {
        binding.share = share
        binding.executePendingBindings()
        binding.lifecycleOwner = lifecycleOwner
        binding.btnOptions.setOnClickListener { listener.onOptionsClick(share) }
        binding.btnApprove.setOnClickListener { listener.onApproveClick(share) }
        binding.btnDeny.setOnClickListener { listener.onDenyClick(share) }
    }
}