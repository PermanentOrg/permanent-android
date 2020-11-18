package org.permanent.permanent.ui.myFiles.upload

import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemUploadBinding
import org.permanent.permanent.models.Upload
import org.permanent.permanent.ui.myFiles.CancelListener

class UploadViewHolder(
    private val binding: ItemUploadBinding,
    private val cancelListener: CancelListener
)
    : RecyclerView.ViewHolder(binding.root) {

    fun bind(upload: Upload, lifecycleOwner: LifecycleOwner) {
        binding.upload = upload
        binding.executePendingBindings()
        binding.lifecycleOwner = lifecycleOwner
        binding.btnCancel.setOnClickListener { cancelListener.onCancelClick(upload) }
    }
}