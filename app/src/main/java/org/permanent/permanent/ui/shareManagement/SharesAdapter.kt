package org.permanent.permanent.ui.shareManagement

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemShareBinding
import org.permanent.permanent.models.Share

class SharesAdapter(shares: List<Share>?, val listener: ShareListener) :
    RecyclerView.Adapter<ShareViewHolder>() {
    private var shares: MutableList<Share> =
        shares?.let { ArrayList(it.toMutableList()) } ?: ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShareViewHolder {
        val binding = ItemShareBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ShareViewHolder(binding, listener)
    }

    fun remove(share: Share) {
        shares.remove(share)
        notifyDataSetChanged()
    }

    fun add(share: Share) {
        shares.add(share)
        notifyDataSetChanged()
    }

    fun update(share: Share) {
        shares.remove(share)
        shares.add(share)
        notifyDataSetChanged()
    }

    override fun getItemCount() = shares.size

    override fun onBindViewHolder(holder: ShareViewHolder, position: Int) {
        holder.bind(shares[position])
    }
}