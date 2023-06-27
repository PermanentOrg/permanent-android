package org.permanent.permanent.ui.myFiles

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemGridRecordBinding
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.shares.PreviewState

class RecordsGridAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val showMyFilesSimplified: Boolean,
    private val isRelocateMode: MutableLiveData<Boolean>,
    private val isSelectMode: MutableLiveData<Boolean>,
    private val previewState: MutableLiveData<PreviewState>,
    private val isForSharePreviewScreen: Boolean,
    private val isForSharesScreen: Boolean,
    private val recordListener: RecordListener
) : RecordsAdapter() {
    private var records: MutableList<Record> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordGridViewHolder {
        val binding = ItemGridRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecordGridViewHolder(
            parent.context,
            binding,
            showMyFilesSimplified,
            isForSharePreviewScreen,
            isForSharesScreen,
            recordListener
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as RecordGridViewHolder
        holder.bind(records[position], lifecycleOwner, previewState)
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
}