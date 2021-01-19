package org.permanent.permanent.ui.shares

import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemShareBinding
import org.permanent.permanent.ui.myFiles.download.DownloadableRecord

class ShareViewHolder (
    private val binding: ItemShareBinding, private val listener: DownloadableRecordListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(record: DownloadableRecord,  lifecycleOwner: LifecycleOwner) {
        binding.record = record
        binding.executePendingBindings()
        binding.lifecycleOwner = lifecycleOwner
        binding.btnOptions.setOnClickListener { listener.onRecordOptionsClick(record) }
    }
}