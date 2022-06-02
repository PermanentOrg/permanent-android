package org.permanent.permanent.ui.public

import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemPublicArchiveBinding
import org.permanent.permanent.models.Archive

class PublicArchiveViewHolder(
    private val binding: ItemPublicArchiveBinding,
    private val listener: PublicArchiveListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(archive: Archive) {
        binding.archive = archive
        binding.executePendingBindings()
        binding.root.setOnClickListener { listener.onArchiveClick(archive)}
        binding.btnShare.setOnClickListener { listener.onShareClick(archive) }
    }
}