package org.permanent.permanent.ui.archives

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemArchiveBinding
import org.permanent.permanent.models.Archive

class ArchiveViewHolder (
    private val binding: ItemArchiveBinding,
    private val listener: ArchiveListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(archive: Archive, showScreenSimplified: Boolean, defaultArchiveId: Int) {
        binding.archive = archive
        binding.executePendingBindings()
        binding.root.setOnClickListener { listener.onArchiveClick(archive) }
        binding.btnOptions.setOnClickListener { listener.onOptionsBtnClick(archive) }
        when {
            archive.id == defaultArchiveId -> {
                binding.btnOptions.visibility = View.INVISIBLE
                binding.btnDefaultArchive.visibility = View.VISIBLE
            }
            showScreenSimplified -> {
                binding.btnOptions.visibility = View.INVISIBLE
                binding.btnDefaultArchive.visibility = View.GONE
            }
            else -> {
                binding.btnOptions.visibility = View.VISIBLE
                binding.btnDefaultArchive.visibility = View.GONE
            }
        }
    }
}