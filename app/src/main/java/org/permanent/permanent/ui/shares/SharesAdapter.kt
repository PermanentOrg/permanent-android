package org.permanent.permanent.ui.shares

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemShareBinding
import org.permanent.permanent.ui.myFiles.download.DownloadableRecord

class SharesAdapter(
    private val listener: DownloadableRecordListener,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<ShareViewHolder>() {
    private var shares: MutableList<DownloadableRecord> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShareViewHolder {
        val binding = ItemShareBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ShareViewHolder(binding, listener)
    }

    fun set(records: MutableList<DownloadableRecord>) {
        shares = records
        notifyDataSetChanged()
    }

    override fun getItemCount() = shares.size

    override fun onBindViewHolder(holder: ShareViewHolder, position: Int) {
        holder.bind(shares[position], lifecycleOwner)
    }

    fun getItemById(recordId: Int): DownloadableRecord? {
        for (share in shares) {
            if (share.id == recordId) return share
        }
        return null
    }
}