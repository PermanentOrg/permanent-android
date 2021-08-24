package org.permanent.permanent.ui.archives

import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemArchiveBinding
import org.permanent.permanent.models.Archive

class ArchiveViewHolder (
    private val binding: ItemArchiveBinding,
    private val listener: ArchiveListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(archive: Archive) {
        binding.archive = archive
        binding.executePendingBindings()
        binding.root.setOnClickListener { listener.onArchiveClick(archive) }
    }
}