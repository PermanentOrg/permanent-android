package org.permanent.permanent.ui.myFiles

import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemUploadBinding
import org.permanent.permanent.models.Upload

class UploadViewHolder(
    private val binding: ItemUploadBinding,
    private val uploadCancelClickListener: UploadCancelClickListener)
    : RecyclerView.ViewHolder(binding.root) {

    fun bind(upload: Upload) {
        binding.upload = upload
        binding.executePendingBindings()
        binding.btnCancel.setOnClickListener { uploadCancelClickListener.onCancelClick(upload) }
    }
}