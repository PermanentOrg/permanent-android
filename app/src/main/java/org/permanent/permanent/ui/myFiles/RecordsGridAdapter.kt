package org.permanent.permanent.ui.myFiles

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemGridRecordBinding
import org.permanent.permanent.models.Record

class RecordsGridAdapter(
    private val lifecycleOwner: LifecycleOwner,
) : RecyclerView.Adapter<RecordGridViewHolder>() {
    private var records: MutableList<Record> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordGridViewHolder {
        val binding = ItemGridRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecordGridViewHolder(binding)
    }

    fun set(recordList: List<Record>) {
        records = recordList.toMutableList()
        notifyDataSetChanged()
    }

    override fun getItemCount() = records.size

    override fun onBindViewHolder(holder: RecordGridViewHolder, position: Int) {
        holder.bind(records[position], lifecycleOwner)
    }
}