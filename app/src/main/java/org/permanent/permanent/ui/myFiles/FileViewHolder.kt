package org.permanent.permanent.ui.myFiles

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.permanent.databinding.FileBinding
import org.permanent.permanent.models.File


class FileViewHolder(private val binding: FileBinding, val onMoreClickListener: OnMoreClickListener) : RecyclerView.ViewHolder(binding.root) {

    fun bind(file: File) {
        binding.file = file
        binding.executePendingBindings()
        binding.ivMore.setOnClickListener { view ->
            onMoreClickListener.onMoreClick()
        }
    }


}