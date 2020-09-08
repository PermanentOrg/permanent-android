package org.permanent.permanent.ui.mainScreen

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class SharesViewPagerAdapter(fragment: Fragment):FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        val fragment:Fragment

        fragment = when(position){
            0 -> SharedByMeFragment()
            1 -> SharedWithMeFragment()
            else -> SharedByMeFragment()
        }
        return fragment
    }
}