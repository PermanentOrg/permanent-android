package org.permanent.permanent.ui.shares

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentSharesBinding
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.myFiles.download.DownloadableRecord
import org.permanent.permanent.viewmodels.SharesViewModel

class SharesFragment : PermanentBaseFragment() {

    private lateinit var binding: FragmentSharesBinding
    private lateinit var viewAdapter: SharesViewPagerAdapter
    private lateinit var viewModel: SharesViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(SharesViewModel::class.java)
        binding = FragmentSharesBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewAdapter = SharesViewPagerAdapter(this)
        val viewPager = binding.viewPager
        viewPager.adapter = viewAdapter

        TabLayoutMediator(binding.tabs, viewPager) { tab, position ->
            when (position) {
                Constants.POSITION_SHARED_BY_ME_FRAGMENT -> tab.text =
                    getString(R.string.shared_by_me_tab_name_)
                Constants.POSITION_SHARED_WITH_ME_FRAGMENT -> tab.text =
                    getString(R.string.shared_with_me_tab_name)
                else -> tab.text = getString(R.string.shared_by_me_tab_name_)
            }
        }.attach()

        arguments?.takeIf { it.containsKey(SELECTED_FRAGMENT_POSITION_KEY) }?.apply {
            viewPager.post {
                viewPager.currentItem = getInt(SELECTED_FRAGMENT_POSITION_KEY)
                viewAdapter.setRecordToNavigateTo(getInt(RECORD_TO_NAVIGATE_TO_KEY))
            }
        }
    }

    private val onShowMessage = Observer<String> { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private val onSharesByMeRetrieved = Observer<MutableList<DownloadableRecord>> {
        viewAdapter.setSharesByMe(it)
    }

    private val onSharesWithMeRetrieved = Observer<MutableList<DownloadableRecord>> {
        viewAdapter.setSharesWithMe(it)
    }

    private val onShareByMeFragmentReady = Observer<Void> {
        viewAdapter.sharedByMeFragment?.getRootShares()?.observe(this, onGetRootShares)
    }

    private val onShareWithMeFragmentReady = Observer<Void> {
        viewAdapter.sharedWithMeFragment?.getRootShares()?.observe(this, onGetRootShares)
    }

    private val onGetRootShares = Observer<Void> {
        viewModel.getShares()
    }

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onShowMessage)
        viewModel.getOnSharesByMeRetrieved().observe(this, onSharesByMeRetrieved)
        viewModel.getOnSharesWithMeRetrieved().observe(this, onSharesWithMeRetrieved)
        viewAdapter.getOnShareByMeFragmentReady().observe(this, onShareByMeFragmentReady)
        viewAdapter.getOnShareWithMeFragmentReady().observe(this, onShareWithMeFragmentReady)
        viewAdapter.sharedByMeFragment?.getRootShares()?.observe(this, onGetRootShares)
        viewAdapter.sharedWithMeFragment?.getRootShares()?.observe(this, onGetRootShares)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getOnSharesByMeRetrieved().removeObserver(onSharesByMeRetrieved)
        viewModel.getOnSharesWithMeRetrieved().removeObserver(onSharesWithMeRetrieved)
        viewAdapter.getOnShareByMeFragmentReady().removeObserver(onShareByMeFragmentReady)
        viewAdapter.getOnShareWithMeFragmentReady().removeObserver(onShareWithMeFragmentReady)
        viewAdapter.sharedByMeFragment?.getRootShares()?.removeObserver(onGetRootShares)
        viewAdapter.sharedWithMeFragment?.getRootShares()?.removeObserver(onGetRootShares)
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }
}