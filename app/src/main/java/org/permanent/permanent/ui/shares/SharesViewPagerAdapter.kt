package org.permanent.permanent.ui.shares

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.ui.myFiles.download.DownloadableRecord
import org.permanent.permanent.viewmodels.SingleLiveEvent

const val NUMBER_OF_FRAGMENTS = 2
const val SHARED_WITH_ME_ITEM_LIST_KEY = "share_with_me_item_list_key"
const val SHARED_X_ME_NO_ITEMS_MESSAGE_KEY = "share_x_me_no_items_message_key"

class SharesViewPagerAdapter(val fragment: Fragment) : FragmentStateAdapter(fragment) {

    private var recordIdToNavigateTo: Int? = null
    var sharedByMeFragment: SharedXMeFragment? = null
    var sharedWithMeFragment: SharedXMeFragment? = null
    private val onShareByMeFragmentReady = SingleLiveEvent<Void>()
    private val onShareWithMeFragmentReady = SingleLiveEvent<Void>()
    private var sharesWithMe: MutableList<DownloadableRecord> = ArrayList()

    override fun getItemCount(): Int = NUMBER_OF_FRAGMENTS

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            Constants.POSITION_SHARED_WITH_ME_FRAGMENT -> {
                val bundle = bundleOf(
                    SHARED_WITH_ME_ITEM_LIST_KEY to sharesWithMe,
                    SHARED_X_ME_NO_ITEMS_MESSAGE_KEY to fragment.context?.getString(R.string.shares_with_me_no_items)
                )
                sharedWithMeFragment = SharedXMeFragment()
                sharedWithMeFragment?.arguments = bundle
                onShareWithMeFragmentReady.call()
                sharedWithMeFragment!!
            }
            else -> {
                val bundle = bundleOf(
                    SHARED_X_ME_NO_ITEMS_MESSAGE_KEY to fragment.context?.getString(R.string.shares_by_me_no_items)
                )
                sharedByMeFragment = SharedXMeFragment()
                sharedByMeFragment?.arguments = bundle
                onShareByMeFragmentReady.call()
                sharedByMeFragment!!
            }
        }
    }

    fun setSharesByMe(list: MutableList<DownloadableRecord>) {
        sharedByMeFragment?.setShares(list)
    }

    fun setSharesWithMe(list: MutableList<DownloadableRecord>) {
        sharesWithMe = list
        if (sharedWithMeFragment != null) {
            sharedWithMeFragment!!.setShares(sharesWithMe)
            sharedWithMeFragment?.navigateToRecord(recordIdToNavigateTo)
        }
    }

    fun setRecordToNavigateTo(recordId: Int) {
        recordIdToNavigateTo = recordId
    }

    fun getOnShareByMeFragmentReady(): LiveData<Void> {
        return onShareByMeFragmentReady
    }

    fun getOnShareWithMeFragmentReady(): LiveData<Void> {
        return onShareWithMeFragmentReady
    }
}