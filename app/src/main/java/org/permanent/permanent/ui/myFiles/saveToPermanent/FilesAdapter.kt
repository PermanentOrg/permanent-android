package org.permanent.permanent.ui.myFiles.saveToPermanent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemFilePreviewBinding
import org.permanent.permanent.models.File

class FilesAdapter(private val files: ArrayList<File>) : RecyclerView.Adapter<FileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemFilePreviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FileViewHolder(binding)
    }

    override fun getItemCount() = files.size

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(files[position])
    }
}