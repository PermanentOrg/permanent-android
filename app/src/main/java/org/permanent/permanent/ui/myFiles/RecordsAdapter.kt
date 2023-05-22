package org.permanent.permanent.ui.myFiles

import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.models.Record

abstract class RecordsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    abstract fun addRecords(fakeFiles: MutableList<Record>)

    abstract fun setRecords(records: List<Record>)

    abstract fun getRecords(): List<Record>

    abstract fun getItemById(recordId: Int): Record?
}