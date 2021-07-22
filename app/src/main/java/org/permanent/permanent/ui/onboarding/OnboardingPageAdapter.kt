package org.permanent.permanent.ui.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.R
import org.permanent.permanent.databinding.ItemOnboardingPageBinding
import org.permanent.permanent.models.OnboardingPage

class OnboardingPageAdapter : RecyclerView.Adapter<OnboardingPageViewHolder>() {

    private val onboardingPages: ArrayList<OnboardingPage> = arrayListOf(
        OnboardingPage(
            R.drawable.img_onboarding_page_one,
            R.string.onboarding_page_one_title,
            R.string.onboarding_page_one_text
        ),
        OnboardingPage(
            R.drawable.img_onboarding_page_two,
            R.string.onboarding_page_two_title,
            R.string.onboarding_page_two_text
        ),
        OnboardingPage(
            R.drawable.img_onboarding_page_three,
            R.string.onboarding_page_three_title,
            R.string.onboarding_page_three_text
        )
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingPageViewHolder {
        val binding = ItemOnboardingPageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OnboardingPageViewHolder(binding)
    }

    override fun getItemCount() = onboardingPages.size

    override fun onBindViewHolder(holder: OnboardingPageViewHolder, position: Int) {
        holder.bind(onboardingPages[position])
    }
}