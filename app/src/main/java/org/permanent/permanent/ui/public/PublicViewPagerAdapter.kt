package org.permanent.permanent.ui.public

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter


class PublicViewPagerAdapter(val isViewOnlyMode: Boolean, val fragment: Fragment) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return NUMBER_OF_FRAGMENTS
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> {
                return PublicArchiveFragment()
            }
        }
        val fragment = PublicProfileFragment()
        fragment.arguments = bundleOf(IS_VIEW_ONLY_MODE to isViewOnlyMode)
        return fragment
    }

    companion object {
        const val NUMBER_OF_FRAGMENTS = 2
        const val IS_VIEW_ONLY_MODE = "is_view_only_mode"
    }
}


