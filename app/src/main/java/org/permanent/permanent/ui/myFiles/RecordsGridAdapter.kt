package org.permanent.permanent.ui.myFiles

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemGridRecordBinding
import org.permanent.permanent.models.Record
import org.permanent.permanent.viewmodels.SharePreviewViewModel

class RecordsGridAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val viewModel: SharePreviewViewModel,
) : RecyclerView.Adapter<RecordGridViewHolder>() {
    var records: MutableList<Record> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordGridViewHolder {
        val binding = ItemGridRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecordGridViewHolder(parent.context, binding)
    }

    fun set(recordList: List<Record>) {
        records = recordList.toMutableList()
        notifyDataSetChanged()
    }

    override fun getItemCount() = records.size

    override fun onBindViewHolder(holder: RecordGridViewHolder, position: Int) {
        holder.bind(records[position], lifecycleOwner, viewModel.getCurrentState())
    }
}