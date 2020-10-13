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
    private val files: List<RecordVO>,
    private val fileOptionsClickListener: FileOptionsClickListener)
    : RecyclerView.Adapter<FileViewHolder>(), Filterable {

    private var fileFilteredList: MutableList<RecordVO> = files.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemFileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FileViewHolder(binding, fileOptionsClickListener)
    }

    override fun getItemCount() = fileFilteredList.size

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(fileFilteredList[position])
    }

    override fun getFilter(): Filter {
        return object : Filter() {

            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charSearch = charSequence.toString()

                fileFilteredList = if (charSearch.isEmpty()) {
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
                filterResults.values = fileFilteredList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                fileFilteredList = results?.values as ArrayList<RecordVO>
                notifyDataSetChanged()
            }
        }
    }
}