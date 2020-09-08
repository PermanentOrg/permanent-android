package org.permanent.permanent.ui.myFiles

import androidx.recyclerview.widget.RecyclerView
import org.permanent.databinding.FileBinding
import org.permanent.permanent.models.File

class FileViewHolder(private val binding: FileBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(file: File) {
        binding.file = file
        binding.executePendingBindings()
    }
}