package org.permanent.permanent.ui.myFiles

import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemFileBinding
import org.permanent.permanent.network.models.RecordVO

class FileViewHolder(
    private val binding: ItemFileBinding,
    private val fileOptionsClickListener: FileOptionsClickListener)
    : RecyclerView.ViewHolder(binding.root) {

    fun bind(file: RecordVO) {
        binding.file = file
        binding.executePendingBindings()
        binding.btnFileOptions.setOnClickListener {
            fileOptionsClickListener.onFileOptionsClick(file)
        }
    }
}