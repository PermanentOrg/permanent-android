package org.permanent.permanent.ui.shares

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.models.ShareItem
import org.permanent.permanent.network.models.Datum

const val NUMBER_OF_FRAGMENTS = 2
const val PARCELABLE_SHARED_WITH_ME_ITEMS_KEY = "parcelable_share_with_me_items_key"
const val PARCELABLE_SHARED_X_ME_NO_ITEMS_STRING_KEY = "parcelable_share_x_me_no_items_string_key"

class SharesViewPagerAdapter(val fragment: Fragment) : FragmentStateAdapter(fragment) {

    private lateinit var sharedByMeFragment: SharedXMeFragment
    private var sharedWithMeFragment: SharedXMeFragment? = null
    private var sharesByMe: MutableList<ShareItem> = ArrayList()
    private var sharesWithMe: MutableList<ShareItem> = ArrayList()

    override fun getItemCount(): Int = NUMBER_OF_FRAGMENTS

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            Constants.POSITION_SHARED_WITH_ME_FRAGMENT -> {
                val bundle = bundleOf(
                    PARCELABLE_SHARED_WITH_ME_ITEMS_KEY to sharesWithMe,
                    PARCELABLE_SHARED_X_ME_NO_ITEMS_STRING_KEY
                            to fragment.context?.getString(R.string.shares_with_me_no_items)
                )
                sharedWithMeFragment = SharedXMeFragment()
                sharedWithMeFragment?.arguments = bundle
                sharedWithMeFragment!!
            }
            else -> {
                val bundle = bundleOf(
                    PARCELABLE_SHARED_X_ME_NO_ITEMS_STRING_KEY
                            to fragment.context?.getString(R.string.shares_by_me_no_items)
                )
                sharedByMeFragment = SharedXMeFragment()
                sharedByMeFragment.arguments = bundle
                sharedByMeFragment
            }
        }
    }

    fun setSharedArchives(dataList: List<Datum>, userArchiveId: Int) {
        for (datum in dataList) {
            val archive = datum.ArchiveVO
            val items = archive?.ItemVOs

            items?.let {
                for (item in it) {
                    val shareItem = ShareItem(item, archive)
                    if (userArchiveId == archive.archiveId) {
                        sharesByMe.add(shareItem)
                    } else {
                        sharesWithMe.add(shareItem)
                    }
                }
            }
        }
        if (sharesByMe.isNotEmpty()) sharedByMeFragment.set(sharesByMe)
        if (sharedWithMeFragment != null && sharesWithMe.isNotEmpty()) {
            sharedWithMeFragment!!.set(sharesWithMe)
        }
    }
}