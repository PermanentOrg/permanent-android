package org.permanent.permanent.ui.myFiles

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.CurrentArchivePermissionsManager
import org.permanent.permanent.databinding.ItemListRecordBinding
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType

class RecordListViewHolder(
    val binding: ItemListRecordBinding,
    private val showMyFilesSimplified: Boolean,
    private val isForSharesScreen: Boolean,
    private val isForSearchScreen: Boolean,
    private val recordListener: RecordListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(record: Record, lifecycleOwner: LifecycleOwner) {
        binding.record = record
        binding.executePendingBindings()
        binding.lifecycleOwner = lifecycleOwner
        binding.btnOptions.visibility =
            if (CurrentArchivePermissionsManager.instance.getAccessRole() == AccessRole.VIEWER && record.type == RecordType.FOLDER || isForSearchScreen || showMyFilesSimplified) View.INVISIBLE else View.VISIBLE
        binding.btnOptions.setOnClickListener {
            if (record.isSelectMode?.value == true) {
                record.isChecked?.value = !record.isChecked?.value!!
                recordListener.onRecordCheckBoxClick(record)
            } else {
                recordListener.onRecordOptionsClick(record)
            }
        }
        binding.layoutOverlay.setOnClickListener {
            if (record.isSelectMode?.value == true) {
                record.isChecked?.value = !record.isChecked?.value!!
                recordListener.onRecordCheckBoxClick(record)
            } else {
                recordListener.onRecordClick(record)
            }
        }
        binding.layoutSwipeReveal.setLockDrag(record.isProcessing || isForSharesScreen || isForSearchScreen)
        binding.btnDelete.setOnClickListener {
            recordListener.onRecordDeleteClick(record)
            binding.layoutSwipeReveal.close(true)
        }
        binding.btnMore.setOnClickListener {
            recordListener.onRecordOptionsClick(record)
            binding.layoutSwipeReveal.close(true)
        }
    }
}