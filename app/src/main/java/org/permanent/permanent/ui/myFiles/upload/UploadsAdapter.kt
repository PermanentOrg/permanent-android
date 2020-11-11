package org.permanent.permanent.ui.myFiles.upload

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemUploadBinding
import org.permanent.permanent.models.Upload

class UploadsAdapter(
    val lifecycleOwner: LifecycleOwner,
    private val uploadCancelListener: UploadCancelListener
) : RecyclerView.Adapter<UploadViewHolder>() {
    private val existsUploads = MutableLiveData(false)
    private var uploads: MutableList<Upload> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UploadViewHolder {
        val binding = ItemUploadBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UploadViewHolder(binding, uploadCancelListener)
    }

    fun set(uploads: MutableList<Upload>) {
        this.uploads = uploads
        existsUploads.value = this.uploads.isNotEmpty()
        notifyDataSetChanged()
    }

    override fun getItemCount() = uploads.size

    override fun onBindViewHolder(holder: UploadViewHolder, position: Int) {
        holder.bind(uploads[position], lifecycleOwner)
    }

    fun remove(upload: Upload?) {
        uploads.remove(upload)
        existsUploads.value = uploads.isNotEmpty()
        notifyDataSetChanged()
    }

    fun getExistsUploads(): MutableLiveData<Boolean> {
        return existsUploads
    }
}