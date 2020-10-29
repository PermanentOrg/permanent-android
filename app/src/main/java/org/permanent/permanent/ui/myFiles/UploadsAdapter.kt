package org.permanent.permanent.ui.myFiles

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemUploadBinding
import org.permanent.permanent.models.Upload

class UploadsAdapter(
    val context: Context,
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

    fun set(uriList: List<Uri>): List<Upload> {
        uploads = getUploads(uriList)
        notifyDataSetChanged()
        return uploads
    }

    private fun getUploads(uriList: List<Uri>): MutableList<Upload> {
        val uploadList: MutableList<Upload> = ArrayList()

        for (uri in uriList) {
            uploadList.add(Upload(context, uri))
        }
        return uploadList
    }

    fun getUploads(): List<Upload> {
        return uploads
    }

    override fun getItemCount() = uploads.size

    override fun onBindViewHolder(holder: UploadViewHolder, position: Int) {
        holder.bind(uploads[position])
    }

    fun remove(upload: Upload?) {
        uploads.remove(upload)
        notifyDataSetChanged()
    }
}