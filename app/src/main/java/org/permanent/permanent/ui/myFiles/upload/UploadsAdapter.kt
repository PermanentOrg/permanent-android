package org.permanent.permanent.ui.myFiles.upload

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemUploadBinding
import org.permanent.permanent.models.Upload
import kotlin.collections.ArrayList

class UploadsAdapter(
    val context: Context,
    val lifecycleOwner: LifecycleOwner,
    private val uploadCancelClickListener: UploadCancelClickListener
) : RecyclerView.Adapter<UploadViewHolder>() {

    private var uploads: MutableList<Upload> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UploadViewHolder {
        val binding = ItemUploadBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UploadViewHolder(binding, uploadCancelClickListener)
    }

    fun set(uploads: MutableList<Upload>) {
        this.uploads = uploads
        notifyDataSetChanged()
    }

    override fun getItemCount() = uploads.size

    override fun onBindViewHolder(holder: UploadViewHolder, position: Int) {
        holder.bind(uploads[position], lifecycleOwner)
    }

    fun remove(upload: Upload?) {
        uploads.remove(upload)
        notifyDataSetChanged()
    }

    fun isEmpty(): Boolean {
        return uploads.isEmpty()
    }
}