package org.permanent.permanent.ui.myFiles

import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.models.Record

interface UnbindableRow {
    fun unbind()
}

abstract class RecordsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // A row the list drops must stop observing the screen's select mode, or it leaks.
    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        (holder as? UnbindableRow)?.unbind()
    }

    abstract fun addRecords(fakeFiles: MutableList<Record>)

    abstract fun setRecords(records: List<Record>)

    abstract fun appendRecords(newRecords: List<Record>)

    abstract fun getRecords(): List<Record>

    abstract fun getItemById(recordId: Int): Record?
}