package org.permanent.permanent.ui.public

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemPublicArchiveBinding
import org.permanent.permanent.models.Archive

class PublicArchiveAdapter(
    private val publicArchiveListener: PublicArchiveListener
) : RecyclerView.Adapter<PublicArchiveViewHolder>() {
    private var archives: MutableList<Archive> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PublicArchiveViewHolder {
        val binding = ItemPublicArchiveBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PublicArchiveViewHolder(binding, publicArchiveListener)
    }

    fun set(archives: List<Archive>) {
        this.archives = archives.toMutableList()
        notifyDataSetChanged()
    }

    override fun getItemCount() = archives.size

    override fun onBindViewHolder(holder: PublicArchiveViewHolder, position: Int) {
        holder.bind(archives[position])
    }
}