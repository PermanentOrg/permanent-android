package org.permanent.permanent.ui.archiveOnboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemArchiveOnboardingBinding
import org.permanent.permanent.models.Archive

class OnboardingArchivesAdapter(private val listener: OnboardingArchiveListener) :
    RecyclerView.Adapter<OnboardingArchiveViewHolder>() {
    private var archives: MutableList<Archive> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingArchiveViewHolder {
        val binding = ItemArchiveOnboardingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OnboardingArchiveViewHolder(binding, listener)
    }

    fun set(archives: List<Archive>) {
        this.archives = archives.toMutableList()
        notifyDataSetChanged()
    }

    override fun getItemCount() = archives.size

    override fun onBindViewHolder(holder: OnboardingArchiveViewHolder, position: Int) {
        holder.bind(archives[position])
    }
}