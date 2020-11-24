package org.permanent.permanent.ui.myFiles

import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemRecordBinding
import org.permanent.permanent.models.Record

class RecordViewHolder(
    private val binding: ItemRecordBinding,
    private val recordClickListener: RecordClickListener,
    private val recordOptionsClickListener: RecordOptionsClickListener)
    : RecyclerView.ViewHolder(binding.root) {

    fun bind(record: Record, lifecycleOwner: LifecycleOwner) {
        binding.record = record
        binding.executePendingBindings()
        binding.lifecycleOwner = lifecycleOwner
        binding.btnOptions.setOnClickListener { recordOptionsClickListener.onRecordOptionsClick(record) }
        binding.root.setOnClickListener { recordClickListener.onRecordClick(record) }
    }
}