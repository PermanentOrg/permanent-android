package org.permanent.permanent.ui.myFiles

import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_list_record.view.*
import kotlinx.android.synthetic.main.item_record_underlay.view.*
import org.permanent.permanent.databinding.ItemListRecordBinding
import org.permanent.permanent.models.Record

class RecordListViewHolder(
    val binding: ItemListRecordBinding, private val recordListener: RecordListener
): RecyclerView.ViewHolder(binding.root) {

    fun bind(record: Record, lifecycleOwner: LifecycleOwner) {
        binding.record = record
        binding.executePendingBindings()
        binding.lifecycleOwner = lifecycleOwner
        binding.btnOptions.setOnClickListener { recordListener.onRecordOptionsClick(record) }
        binding.layoutOverlay.setOnClickListener { recordListener.onRecordClick(record) }
        binding.layoutSwipeReveal.layoutUnderlay.getChildAt(0).btnDelete
            .setOnClickListener {
                recordListener.onRecordDeleteFromSwipeClick(record)
                binding.layoutSwipeReveal.close(true)
            }
        binding.layoutSwipeReveal.layoutUnderlay.getChildAt(0).btnMore
            .setOnClickListener {
                recordListener.onRecordOptionsClick(record)
                binding.layoutSwipeReveal.close(true)
            }
    }
}