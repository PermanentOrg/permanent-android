package org.permanent.permanent.ui.myFiles

import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemGridRecordBinding
import org.permanent.permanent.models.Record

class RecordGridViewHolder(val binding: ItemGridRecordBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(record: Record, lifecycleOwner: LifecycleOwner) {
        binding.record = record
        binding.executePendingBindings()
        binding.lifecycleOwner = lifecycleOwner
    }
}