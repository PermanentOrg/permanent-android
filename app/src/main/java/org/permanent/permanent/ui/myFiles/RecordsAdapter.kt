package org.permanent.permanent.ui.myFiles

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.ViewBinderHelper
import org.permanent.permanent.databinding.ItemRecordBinding
import org.permanent.permanent.models.Record
import java.util.*
import kotlin.collections.ArrayList

class RecordsAdapter(
    private val recordListener: RecordListener,
    private val lifecycleOwner: LifecycleOwner,
    private val isRelocateMode: MutableLiveData<Boolean>
) : RecyclerView.Adapter<RecordViewHolder>(), Filterable {
    private var records: MutableList<Record> = ArrayList()
    private var filteredRecords: MutableList<Record> = ArrayList()
    private val viewBinderHelper = ViewBinderHelper()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val binding = ItemRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecordViewHolder(binding, recordListener)
    }

    fun set(recordList: List<Record>) {
        records = recordList.toMutableList()
        for (record in records) record.isRelocateMode = isRelocateMode
        filteredRecords = records
        notifyDataSetChanged()
    }

    fun add(fakeFile: Record) {
        records.add(fakeFile)
        fakeFile.isRelocateMode = isRelocateMode
        filteredRecords = records
        notifyDataSetChanged()
    }

    override fun getItemCount() = filteredRecords.size

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = filteredRecords[position]
        viewBinderHelper.bind(holder.binding.layoutSwipeReveal, record.folderLinkId.toString())
        holder.bind(record, lifecycleOwner)
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