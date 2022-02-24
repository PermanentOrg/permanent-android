package org.permanent.permanent.ui.publicWorkspace

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter


const val NUMBER_OF_FRAGMENTS = 2

class PublicViewPagerAdapter(val fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return NUMBER_OF_FRAGMENTS
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> {
                return PublicArchiveFragment()
            }
        }
        return PublicProfileFragment()
    }
}


