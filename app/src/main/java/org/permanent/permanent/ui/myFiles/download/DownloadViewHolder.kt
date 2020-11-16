package org.permanent.permanent.ui.myFiles.download

import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemDownloadBinding
import org.permanent.permanent.models.Download

class DownloadViewHolder(
    private val binding: ItemDownloadBinding,
    private val downloadCancelListener: DownloadCancelListener)
    : RecyclerView.ViewHolder(binding.root) {

    fun bind(download: Download, lifecycleOwner: LifecycleOwner) {
        binding.download = download
        binding.executePendingBindings()
        binding.lifecycleOwner = lifecycleOwner
        binding.btnCancel.setOnClickListener { downloadCancelListener.onCancelClick(download) }
    }
}