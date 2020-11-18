package org.permanent.permanent.ui.myFiles.download

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemDownloadBinding
import org.permanent.permanent.models.Download
import org.permanent.permanent.ui.myFiles.CancelListener

class DownloadsAdapter(
    val lifecycleOwner: LifecycleOwner,
    private val cancelListener: CancelListener
) : RecyclerView.Adapter<DownloadViewHolder>() {
    private val existsDownloads = MutableLiveData(false)
    private var downloads: MutableList<Download> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadViewHolder {
        val binding = ItemDownloadBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DownloadViewHolder(binding, cancelListener)
    }

    fun set(downloads: MutableList<Download>) {
        this.downloads = downloads
        existsDownloads.value = this.downloads.isNotEmpty()
        notifyDataSetChanged()
    }

    override fun getItemCount() = downloads.size

    override fun onBindViewHolder(holder: DownloadViewHolder, position: Int) {
        holder.bind(downloads[position], lifecycleOwner)
    }

    fun remove(download: Download?) {
        downloads.remove(download)
        existsDownloads.value = downloads.isNotEmpty()
        notifyDataSetChanged()
    }

    fun getExistsDownloads(): MutableLiveData<Boolean> {
        return existsDownloads
    }
}