package org.permanent.permanent.ui.myFiles.linkshare

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemArchiveShareBinding
import org.permanent.permanent.models.Share

class SharesAdapter(val lifecycleOwner: LifecycleOwner, shares: List<Share>?, val listener: ShareListener
) : RecyclerView.Adapter<ShareViewHolder>() {
    private var shares: MutableList<Share> =
        shares?.let { ArrayList(it.toMutableList()) } ?: ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShareViewHolder {
        val binding = ItemArchiveShareBinding.inflate(
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

    override fun getItemCount() = shares.size

    override fun onBindViewHolder(holder: ShareViewHolder, position: Int) {
        holder.bind(shares[position], lifecycleOwner)
    }
}