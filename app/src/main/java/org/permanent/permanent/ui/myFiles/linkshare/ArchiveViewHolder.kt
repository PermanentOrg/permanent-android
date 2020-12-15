package org.permanent.permanent.ui.myFiles.linkshare

import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemArchiveBinding
import org.permanent.permanent.models.Archive

class ArchiveViewHolder (
    private val binding: ItemArchiveBinding,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(archive: Archive) {
        binding.archive = archive
        binding.executePendingBindings()
    }
}