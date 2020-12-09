package org.permanent.permanent.ui.myFiles.linkshare

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemArchiveBinding
import org.permanent.permanent.models.Share

class ArchivesAdapter(shares: List<Share>?) : RecyclerView.Adapter<ArchiveViewHolder>() {
    private var archives: MutableList<Share> = shares?.let { ArrayList(it.toMutableList()) } ?: ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchiveViewHolder {
        val binding = ItemArchiveBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ArchiveViewHolder(binding)
    }

    override fun getItemCount() = archives.size

    override fun onBindViewHolder(holder: ArchiveViewHolder, position: Int) {
        archives[position].archive?.let { holder.bind(it) }
    }
}