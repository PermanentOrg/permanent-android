package org.permanent.permanent.ui.myFiles.saveToPermanent

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemFilePreviewBinding
import org.permanent.permanent.models.File

class FilesAdapter(val files: ArrayList<File>) :
    RecyclerView.Adapter<FileViewHolder>(), FileListener {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemFilePreviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FileViewHolder(binding, this)
    }

    override fun getItemCount() = files.size

    override fun onRemoveBtnClick(file: File) {
        notifyItemRemoved(files.indexOf(file))
        files.remove(file)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(files[position])
    }

    fun getUriList(): List<Uri> = files.map { it.uri }
}