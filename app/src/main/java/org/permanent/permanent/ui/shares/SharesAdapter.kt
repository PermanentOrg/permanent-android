package org.permanent.permanent.ui.shares

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemShareBinding
import org.permanent.permanent.models.Record

class SharesAdapter : RecyclerView.Adapter<ShareViewHolder>() {
    private var shares: MutableList<Record> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShareViewHolder {
        val binding = ItemShareBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ShareViewHolder(binding)
    }

    override fun getItemCount() = shares.size

    override fun onBindViewHolder(holder: ShareViewHolder, position: Int) {
        shares[position]?.let { holder.bind(it) }
    }
}