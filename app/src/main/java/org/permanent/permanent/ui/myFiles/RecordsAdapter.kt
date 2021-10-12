package org.permanent.permanent.ui.myFiles

import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.models.Record

abstract class RecordsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    abstract fun addRecord(fakeFile: Record)

    abstract fun setRecords(records: List<Record>)

    abstract fun getRecords(): List<Record>
}