package org.permanent.permanent.ui.onboarding

import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.ItemOnboardingPageBinding
import org.permanent.permanent.models.OnboardingPage

class OnboardingPageViewHolder(private val binding: ItemOnboardingPageBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(onboardingPage: OnboardingPage) {
        binding.page = onboardingPage
        binding.executePendingBindings()
    }
}