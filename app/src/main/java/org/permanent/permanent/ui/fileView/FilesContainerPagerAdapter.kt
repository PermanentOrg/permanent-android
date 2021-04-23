package org.permanent.permanent.ui.fileView

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.myFiles.PARCELABLE_RECORD_KEY

class FilesContainerPagerAdapter(val fragment: Fragment, private val files: MutableList<Record>
) : FragmentStatePagerAdapter(fragment.parentFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int = files.size

    override fun getItem(position: Int): Fragment {
        val fragment = FileViewFragment()
        val bundle = bundleOf(PARCELABLE_RECORD_KEY to files[position])
        fragment.arguments = bundle
        return fragment
    }
}