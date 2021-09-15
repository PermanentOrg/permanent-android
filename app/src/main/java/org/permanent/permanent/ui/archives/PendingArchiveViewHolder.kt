package org.permanent.permanent.ui.archives

import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemArchiveBinding
import org.permanent.permanent.models.Archive

class PendingArchiveViewHolder(
    private val binding: ItemArchiveBinding,
    private val listener: PendingArchiveListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(archive: Archive) {
        binding.archive = archive
        binding.executePendingBindings()
        binding.btnAccept.setOnClickListener { listener.onAcceptBtnClick(archive) }
        binding.btnDecline.setOnClickListener { listener.onDeclineBtnClick(archive) }
    }
}