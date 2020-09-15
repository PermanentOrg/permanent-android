package org.permanent.permanent.ui.mainScreen

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.permanent.permanent.Constants

class SharesViewPagerAdapter(fragment: Fragment):FragmentStateAdapter(fragment) {

    private val NUMBER_OF_FRAGMENTS = 2

    override fun getItemCount(): Int = NUMBER_OF_FRAGMENTS

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            Constants.POSITION_SHARED_BY_ME_FRAGMENT -> SharedByMeFragment()
            Constants.POSITION_SHARED_WITH_ME_FRAGMENT -> SharedWithMeFragment()
            else -> SharedByMeFragment()
        }
    }
}