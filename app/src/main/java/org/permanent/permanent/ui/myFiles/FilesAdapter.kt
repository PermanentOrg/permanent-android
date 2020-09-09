package org.permanent.permanent.ui.myFiles

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.permanent.databinding.FileBinding
import org.permanent.permanent.models.File

class FilesAdapter(private val files: List<File>,val onMoreClickListener: OnMoreClickListener) : RecyclerView.Adapter<FileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = FileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding, onMoreClickListener)
    }

    override fun getItemCount() = files.size

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(files[position])
    }
}