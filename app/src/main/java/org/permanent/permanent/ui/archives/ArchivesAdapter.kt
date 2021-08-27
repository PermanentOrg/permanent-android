package org.permanent.permanent.ui.archives

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemArchiveBinding
import org.permanent.permanent.models.Archive

class ArchivesAdapter(
    private val archiveListener: ArchiveListener
) : RecyclerView.Adapter<ArchiveViewHolder>() {
    private var archives: MutableList<Archive> = ArrayList()
    private var defaultArchiveId: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchiveViewHolder {
        val binding = ItemArchiveBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ArchiveViewHolder(binding, archiveListener)
    }

    fun set(archives: List<Archive>, defaultArchiveId: Int) {
        this.archives = archives.toMutableList()
        this.defaultArchiveId = defaultArchiveId
        notifyDataSetChanged()
    }

    override fun getItemCount() = archives.size

    override fun onBindViewHolder(holder: ArchiveViewHolder, position: Int) {
        holder.bind(archives[position], defaultArchiveId)
    }

    fun onDefaultArchiveChanged(archiveId: Int) {
        defaultArchiveId = archiveId
        notifyDataSetChanged()
    }
}