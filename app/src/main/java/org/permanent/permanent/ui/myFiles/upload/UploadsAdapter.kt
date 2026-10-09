package org.permanent.permanent.ui.myFiles.upload

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemUploadBinding
import org.permanent.permanent.models.Upload
import org.permanent.permanent.ui.myFiles.CancelListener

class UploadsAdapter(
    val lifecycleOwner: LifecycleOwner,
    private val cancelListener: CancelListener
) : RecyclerView.Adapter<UploadViewHolder>() {
    private val existsUploads = MutableLiveData(false)
    private var uploads: MutableList<Upload> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UploadViewHolder {
        val binding = ItemUploadBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UploadViewHolder(binding, cancelListener)
    }

    // A copy: the queue removes a finished upload before remove() is called.
    fun set(uploads: MutableList<Upload>) {
        this.uploads = ArrayList(uploads)
        existsUploads.value = this.uploads.isNotEmpty()
        notifyDataSetChanged()
    }

    override fun getItemCount() = uploads.size

    override fun onBindViewHolder(holder: UploadViewHolder, position: Int) {
        holder.bind(uploads[position], lifecycleOwner)
    }

    fun remove(upload: Upload?) {
        val index = uploads.indexOf(upload)
        if (index < 0) return
        uploads.removeAt(index)
        existsUploads.value = uploads.isNotEmpty()
        notifyItemRemoved(index)
    }

    fun getExistsUploads(): MutableLiveData<Boolean> {
        return existsUploads
    }
}