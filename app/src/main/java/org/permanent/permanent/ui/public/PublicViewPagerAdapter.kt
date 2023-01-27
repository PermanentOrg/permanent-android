package org.permanent.permanent.ui.public

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.permanent.permanent.models.Archive
import org.permanent.permanent.ui.public.PublicFragment.Companion.ARCHIVE
import org.permanent.permanent.ui.public.PublicFragment.Companion.ARCHIVE_NR
import org.permanent.permanent.ui.public.PublicFragment.Companion.FILE_ARCHIVE_NR
import org.permanent.permanent.ui.public.PublicFragment.Companion.FOLDER_ARCHIVE_NR
import org.permanent.permanent.ui.public.PublicFragment.Companion.FOLDER_LINK_ID


class PublicViewPagerAdapter(
    val isViewOnlyMode: Boolean,
    val archive: Archive,
    val fileArchiveNr: String?,
    val folderLinkId: String? = null,
    val folderArchiveNr: String? = null,
    val fragment: Fragment
) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return NUMBER_OF_FRAGMENTS
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> {
                return PublicArchiveFragment().apply {
                    arguments = bundleOf(
                        ARCHIVE_NR to archive.number,
                        FILE_ARCHIVE_NR to fileArchiveNr,
                        FOLDER_LINK_ID to folderLinkId,
                        FOLDER_ARCHIVE_NR to folderArchiveNr
                    )
                }
            }
        }
        return PublicProfileFragment().apply {
            arguments = bundleOf(IS_VIEW_ONLY_MODE to isViewOnlyMode, ARCHIVE to archive)
        }
    }

    companion object {
        const val NUMBER_OF_FRAGMENTS = 2
        const val IS_VIEW_ONLY_MODE = "is_view_only_mode"
    }
}


