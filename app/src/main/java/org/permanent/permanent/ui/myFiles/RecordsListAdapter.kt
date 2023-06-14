package org.permanent.permanent.ui.myFiles

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.ViewBinderHelper
import org.permanent.permanent.databinding.ItemListRecordBinding
import org.permanent.permanent.models.Record

class RecordsListAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val showMyFilesSimplified: Boolean,
    private val isRelocateMode: MutableLiveData<Boolean>,
    private val isSelectMode: MutableLiveData<Boolean>,
    private val isForSharesScreen: Boolean,
    private val isForSearchScreen: Boolean,
    private val recordListener: RecordListener
) : RecordsAdapter() {
    private var records: MutableList<Record> = ArrayList()
    private val viewBinderHelper = ViewBinderHelper()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordListViewHolder {
        val binding = ItemListRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecordListViewHolder(binding, showMyFilesSimplified, isForSharesScreen, isForSearchScreen, recordListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val record = records[position]
        holder as RecordListViewHolder
        viewBinderHelper.bind(holder.binding.layoutSwipeReveal, record.folderLinkId.toString())
        holder.bind(record, lifecycleOwner)
    }

    override fun getItemCount() = records.size

    override fun setRecords(records: List<Record>) {
        this.records = records.toMutableList()
        for (record in this.records) {
            record.isRelocateMode = isRelocateMode
            record.isSelectMode = isSelectMode
            record.isChecked = MutableLiveData<Boolean>(false)
        }
        notifyDataSetChanged()
    }

    override fun getRecords(): List<Record> = records

    override fun getItemById(recordId: Int): Record? {
        for (record in records) {
            if (record.id == recordId) return record
        }
        return null
    }

    override fun addRecords(fakeFiles: MutableList<Record>) {
        for ((index, fakeFile) in fakeFiles.withIndex()) {
            fakeFile.isRelocateMode = isRelocateMode
            fakeFile.isSelectMode = isSelectMode
            fakeFile.isChecked = MutableLiveData<Boolean>(false)
            records.add(index, fakeFile)
        }
        notifyDataSetChanged()
    }

    fun updateNameOfRecord(recordId: Int?, recordName: String?) {
        for (record in records) {
            if (record.id == recordId) {
                record.displayName = recordName
                notifyDataSetChanged()
            }
        }
    }
}