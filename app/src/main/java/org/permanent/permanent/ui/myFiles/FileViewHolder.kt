package org.permanent.permanent.ui.myFiles

import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemFileBinding
import org.permanent.permanent.models.File

class FileViewHolder(
    private val binding: ItemFileBinding,
    private val fileOptionsClickListener: FileOptionsClickListener)
    : RecyclerView.ViewHolder(binding.root) {

    fun bind(file: File) {
        binding.file = file
        binding.executePendingBindings()
        binding.btnFileOptions.setOnClickListener {
            fileOptionsClickListener.onFileOptionsClick(file)
        }
    }
}