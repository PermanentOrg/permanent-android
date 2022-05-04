package org.permanent.permanent.ui.public

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter


const val NUMBER_OF_FRAGMENTS = 2

class PublicViewPagerAdapter(val fragment: Fragment, archiveNr: String?) : FragmentStateAdapter(fragment) {

    private val bundle = bundleOf(PublicProfileFragment.ARCHIVE_NR to archiveNr)
    override fun getItemCount(): Int {
        return NUMBER_OF_FRAGMENTS
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> {
                return PublicArchiveFragment().apply {
                    arguments = bundle
                }
            }
        }
        return PublicProfileFragment().apply {
            arguments = bundle
        }
    }

    companion object {
        const val ARCHIVE_NR = "archive_nr"
    }
}


