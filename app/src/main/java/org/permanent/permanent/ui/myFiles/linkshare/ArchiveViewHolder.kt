package org.permanent.permanent.ui.myFiles.linkshare

import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemArchiveBinding
import org.permanent.permanent.models.Archive
import org.permanent.permanent.models.Status

class ArchiveViewHolder(
    private val binding: ItemArchiveBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(archive: Archive, status: Status?) {
        binding.archive = archive
        binding.isPending = status?.name.equals("PENDING") //TODO remove hardcoded status
        binding.executePendingBindings()
    }
}