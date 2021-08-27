package org.permanent.permanent.ui.archives

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemArchiveBinding
import org.permanent.permanent.models.Archive

class ArchiveViewHolder (
    private val binding: ItemArchiveBinding,
    private val listener: ArchiveListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(archive: Archive, defaultArchiveId: Int) {
        binding.archive = archive
        binding.executePendingBindings()
        binding.root.setOnClickListener { listener.onArchiveClick(archive) }
        binding.btnOptions.setOnClickListener { listener.onOptionsBtnClick(archive) }
        if (archive.id == defaultArchiveId) {
            binding.btnOptions.visibility = View.INVISIBLE
            binding.btnDefaultArchive.visibility = View.VISIBLE
        } else {
            binding.btnOptions.visibility = View.VISIBLE
            binding.btnDefaultArchive.visibility = View.GONE
        }
    }
}