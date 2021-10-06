package org.permanent.permanent.ui.myFiles

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.ViewBinderHelper
import org.permanent.permanent.databinding.ItemListRecordBinding
import org.permanent.permanent.models.Record
import java.util.*
import kotlin.collections.ArrayList

class RecordsListAdapter(
    private val recordListener: RecordListener,
    private val lifecycleOwner: LifecycleOwner,
    private val isRelocateMode: MutableLiveData<Boolean>
) : RecordsAdapter() {
    private var records: MutableList<Record> = ArrayList()
    private var filteredRecords: MutableList<Record> = ArrayList()
    private val viewBinderHelper = ViewBinderHelper()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordListViewHolder {
        val binding = ItemListRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecordListViewHolder(binding, recordListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val record = filteredRecords[position]
        holder as RecordListViewHolder
        viewBinderHelper.bind(holder.binding.layoutSwipeReveal, record.folderLinkId.toString())
        holder.bind(record, lifecycleOwner)
    }

    override fun getItemCount() = filteredRecords.size

    override fun setRecords(records: List<Record>) {
        this.records = records.toMutableList()
        for (record in this.records) record.isRelocateMode = isRelocateMode
        filteredRecords = this.records
        notifyDataSetChanged()
    }

    override fun getRecords(): List<Record> = records

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
                                .contains(charSearch.toLowerCase(Locale.ROOT))) {
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