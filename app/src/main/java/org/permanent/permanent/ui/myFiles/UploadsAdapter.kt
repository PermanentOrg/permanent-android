package org.permanent.permanent.ui.myFiles

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemUploadBinding
import org.permanent.permanent.models.Upload
import java.util.*
import kotlin.collections.ArrayList

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
        uploads = uriList.map { Upload(context, it) }.toMutableList()
        notifyDataSetChanged()
        return uploads
    }

    fun getUploadById(id: UUID): Upload? {
        for(upload in uploads) {
            if (upload.uuid == id) return upload
        }
        return null
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