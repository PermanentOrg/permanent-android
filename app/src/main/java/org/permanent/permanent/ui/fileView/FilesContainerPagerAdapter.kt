package org.permanent.permanent.ui.fileView

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.myFiles.PARCELABLE_RECORD_KEY

class FilesContainerPagerAdapter(val fragment: Fragment, private val files: MutableList<Record>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = files.size

    override fun createFragment(position: Int): Fragment {
        val fragment = FileViewFragment()
        val bundle = bundleOf(PARCELABLE_RECORD_KEY to files[position])
        fragment.arguments = bundle
        return fragment
    }
}