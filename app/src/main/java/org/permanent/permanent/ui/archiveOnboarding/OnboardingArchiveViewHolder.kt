package org.permanent.permanent.ui.archiveOnboarding

import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemArchiveOnboardingBinding
import org.permanent.permanent.models.Archive

class OnboardingArchiveViewHolder(
    private val binding: ItemArchiveOnboardingBinding,
    private val listener: OnboardingArchiveListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(archive: Archive) {
        binding.archive = archive
        binding.executePendingBindings()
        binding.tvAccept.setOnClickListener { listener.onAcceptBtnClick(archive) }
        binding.root.setOnClickListener { listener.onMakeDefaultBtnClick(archive) }
    }
}