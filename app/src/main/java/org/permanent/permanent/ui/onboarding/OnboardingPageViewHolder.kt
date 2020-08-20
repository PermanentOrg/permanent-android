package org.permanent.permanent.ui.onboarding

import androidx.recyclerview.widget.RecyclerView
import org.permanent.databinding.OnboardingPageBinding
import org.permanent.permanent.models.OnboardingPage

class OnboardingPageViewHolder(private val binding: OnboardingPageBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(onboardingPage: OnboardingPage) {
        binding.page = onboardingPage
        binding.executePendingBindings()
    }
}