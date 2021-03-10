package org.permanent.permanent.ui.fileView

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.permanent.permanent.Constants
import org.permanent.permanent.network.models.FileData

const val NUMBER_OF_FRAGMENTS = 2
class FileMetadataViewPagerAdapter(val fragment: Fragment) : FragmentStateAdapter(fragment) {

    private lateinit var fileData: FileData
    var fileInfoFragment: FileInfoFragment? = null
    var fileDetailsFragment: FileDetailsFragment? = null

    fun setFileData(fileData: FileData) {
        this.fileData = fileData
    }

    override fun createFragment(position: Int): Fragment {
        val bundle = bundleOf(PARCELABLE_FILE_DATA_KEY to fileData)
        return when(position) {
            Constants.POSITION_DETAILS_FRAGMENT -> {
                fileDetailsFragment = FileDetailsFragment()
                fileDetailsFragment?.arguments = bundle
                fileDetailsFragment!!
            }
            else -> {
                fileInfoFragment = FileInfoFragment()
                fileInfoFragment?.arguments = bundle
                fileInfoFragment!!
            }
        }
    }

    override fun getItemCount(): Int = NUMBER_OF_FRAGMENTS
}