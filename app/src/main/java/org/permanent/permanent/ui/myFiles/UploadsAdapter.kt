package org.permanent.permanent.ui.myFiles

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemUploadBinding
import org.permanent.permanent.models.Upload

class UploadsAdapter(private val uploadCancelClickListener: UploadCancelClickListener)
    : RecyclerView.Adapter<UploadViewHolder>() {

    private var uploads: List<Upload> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UploadViewHolder {
        val binding = ItemUploadBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UploadViewHolder(binding, uploadCancelClickListener)
    }

    fun set(uploadList: List<Upload>) {
        uploads = uploadList
        notifyDataSetChanged()
    }

    override fun getItemCount() = uploads.size

    override fun onBindViewHolder(holder: UploadViewHolder, position: Int) {
        holder.bind(uploads[position])
    }
}