package org.permanent.permanent.ui.archives

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemArchiveBinding
import org.permanent.permanent.models.Archive

class PendingArchivesAdapter (
    private val listener: PendingArchiveListener
) : RecyclerView.Adapter<PendingArchiveViewHolder>() {
    private var archives: MutableList<Archive> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingArchiveViewHolder {
        val binding = ItemArchiveBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PendingArchiveViewHolder(binding, listener)
    }

    fun set(archives: List<Archive>) {
        this.archives = archives.toMutableList()
        notifyDataSetChanged()
    }

    override fun getItemCount() = archives.size

    override fun onBindViewHolder(holder: PendingArchiveViewHolder, position: Int) {
        holder.bind(archives[position])
    }
}