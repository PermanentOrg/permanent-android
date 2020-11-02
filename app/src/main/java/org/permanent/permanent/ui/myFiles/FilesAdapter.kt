package org.permanent.permanent.ui.myFiles

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemFileBinding
import org.permanent.permanent.network.models.RecordVO
import java.util.*
import kotlin.collections.ArrayList

class FilesAdapter(
    private val fileClickListener: FileClickListener,
    private val fileOptionsClickListener: FileOptionsClickListener)
    : RecyclerView.Adapter<FileViewHolder>(), Filterable {
    private var files: MutableList<RecordVO> = ArrayList()
    private var filteredList: MutableList<RecordVO> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemFileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FileViewHolder(binding, fileClickListener, fileOptionsClickListener)
    }

    fun set(records: List<RecordVO>) {
        files = records.toMutableList()
        filteredList = files
        notifyDataSetChanged()
    }

    fun add(fakeFile: RecordVO) {
        files.add(fakeFile)
        filteredList = files
        notifyDataSetChanged()
    }

    override fun getItemCount() = filteredList.size

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(filteredList[position])
    }

    override fun getFilter(): Filter {
        return object : Filter() {

            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charSearch = charSequence.toString()

                filteredList = if (charSearch.isEmpty()) {
                    files.toMutableList()
                } else {
                    val resultList = ArrayList<RecordVO>()
                    for (file in files) {
                        if (file.displayName != null && file.displayName!!.toLowerCase(Locale.ROOT)
                                .contains(charSearch.toLowerCase(Locale.ROOT))) {
                            resultList.add(file)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as ArrayList<RecordVO>
                notifyDataSetChanged()
            }
        }
    }
}