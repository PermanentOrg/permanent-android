package org.permanent.permanent.ui.myFiles

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemGridRecordBinding
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.shares.PreviewState
import java.util.*
import kotlin.collections.ArrayList

class RecordsGridAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val isRelocateMode: MutableLiveData<Boolean>,
    private val previewState: MutableLiveData<PreviewState>,
    private val isForSharePreviewScreen: Boolean,
    private val isForSharesScreen: Boolean,
    private val recordListener: RecordListener
) : RecordsAdapter() {
    private var records: MutableList<Record> = ArrayList()
    private var filteredRecords: MutableList<Record> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordGridViewHolder {
        val binding = ItemGridRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecordGridViewHolder(
            parent.context,
            binding,
            isForSharePreviewScreen,
            isForSharesScreen,
            recordListener
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as RecordGridViewHolder
        holder.bind(filteredRecords[position], lifecycleOwner, previewState)
    }

    override fun getItemCount() = filteredRecords.size

    override fun setRecords(records: List<Record>) {
        this.records = records.toMutableList()
        for (record in this.records) record.isRelocateMode = isRelocateMode
        filteredRecords = this.records
        notifyDataSetChanged()
    }

    override fun getRecords(): List<Record> = records

    override fun getItemById(recordId: Int): Record? {
        for (record in records) {
            if (record.id == recordId) return record
        }
        return null
    }

    override fun addRecord(fakeFile: Record) {
        records.add(0, fakeFile)
        fakeFile.isRelocateMode = isRelocateMode
        filteredRecords = records
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {

            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charSearch = charSequence.toString()

                filteredRecords = if (charSearch.isEmpty()) {
                    records.toMutableList()
                } else {
                    val resultList = ArrayList<Record>()
                    for (record in records) {
                        if (record.displayName != null
                            && record.displayName!!.toLowerCase(Locale.ROOT)
                                .contains(charSearch.toLowerCase(Locale.ROOT))
                        ) {
                            resultList.add(record)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredRecords
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredRecords = results?.values as ArrayList<Record>
                notifyDataSetChanged()
            }
        }
    }
}