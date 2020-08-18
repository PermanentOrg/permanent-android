package org.permanent.permanent.ui.onboarding

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_onboarding_page.view.*
import org.permanent.permanent.models.OnboardingPage

class OnboardingPageViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    private val ivImage: ImageView = view.ivOnboardingPage
    private val tvTitle: TextView = view.tvTitleOnboardingPage
    private val tvText: TextView = view.tvTextOnboardingPage

    fun bind(onboardingPage: OnboardingPage) {
        ivImage.setImageResource(onboardingPage.imageDrawableId)
        tvTitle.text = view.context.getString(onboardingPage.titleResId)
        tvText.text = view.context.getString(onboardingPage.textResId)
    }
}