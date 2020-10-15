package org.permanent.permanent.ui.myFiles

import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemFileBinding
import org.permanent.permanent.network.models.RecordVO

class FileViewHolder(
    private val binding: ItemFileBinding,
    private val fileClickListener: FileClickListener,
    private val fileOptionsClickListener: FileOptionsClickListener)
    : RecyclerView.ViewHolder(binding.root) {

    fun bind(file: RecordVO) {
        binding.file = file
        binding.executePendingBindings()
        binding.btnOptions.setOnClickListener { fileOptionsClickListener.onFileOptionsClick(file) }
        binding.root.setOnClickListener { fileClickListener.onFileClick(file) }
    }
}