package org.permanent.permanent.ui.myFiles.saveToPermanent

import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemFilePreviewBinding
import org.permanent.permanent.models.File

class FileViewHolder(private val binding: ItemFilePreviewBinding, val listener: FileListener) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(file: File) {
        binding.file = file
        binding.executePendingBindings()
        binding.btnRemove.setOnClickListener { listener.onRemoveBtnClick(file) }
    }
}