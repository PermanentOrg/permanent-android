package org.permanent.permanent.ui.shares

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentSharesBinding
import org.permanent.permanent.models.AccountEventAction
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.Workspace
import org.permanent.permanent.ui.myFiles.PARCELABLE_FILES_KEY
import org.permanent.permanent.ui.myFiles.PARCELABLE_RECORD_KEY
import org.permanent.permanent.ui.myFiles.saveToPermanent.SaveToPermanentFragment.Companion.WORKSPACE_KEY
import org.permanent.permanent.viewmodels.SharesViewModel
import org.permanent.permanent.viewmodels.SingleLiveEvent

class SharesFragment : PermanentBaseFragment() {

    private lateinit var binding: FragmentSharesBinding
    private lateinit var viewPager2: ViewPager2
    private lateinit var viewPagerAdapter: SharesViewPagerAdapter
    private lateinit var viewModel: SharesViewModel
    private val onRecordSelectedEvent = SingleLiveEvent<Pair<Workspace, Record>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[SharesViewModel::class.java]
        viewModel.sendEvent(AccountEventAction.OPEN_SHARED_WORKSPACE, data = mapOf("workspace" to "Shared Files"))
        binding = FragmentSharesBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.requestShares()
        val showScreenSimplified = arguments?.getBoolean(SHOW_SCREEN_SIMPLIFIED_KEY) ?: false
        viewPagerAdapter = SharesViewPagerAdapter(this, showScreenSimplified)
        viewPager2 = binding.vpShares
        viewPager2.adapter = viewPagerAdapter
        viewPager2.isSaveEnabled = false

        TabLayoutMediator(binding.tlShares, viewPager2) { tab, position ->
            when (position) {
                Constants.POSITION_SHARED_BY_ME_FRAGMENT -> tab.text =
                    getString(R.string.shared_by_me_tab_name_)
                Constants.POSITION_SHARED_WITH_ME_FRAGMENT -> tab.text =
                    getString(R.string.shared_with_me_tab_name)
                else -> tab.text = getString(R.string.shared_by_me_tab_name_)
            }
        }.attach()

        arguments?.takeIf { it.containsKey(CHILD_FRAGMENT_TO_NAVIGATE_TO_KEY) }?.apply {
            viewPager2.post {
                viewPager2.currentItem = getInt(CHILD_FRAGMENT_TO_NAVIGATE_TO_KEY)
                viewPagerAdapter.setRecordToNavigateTo(getInt(RECORD_ID_TO_NAVIGATE_TO_KEY))
                arguments?.clear()
            }
        }

        arguments?.takeIf { it.containsKey(PARCELABLE_FILES_KEY) }?.apply {
            if (getParcelable<Workspace>(WORKSPACE_KEY) == Workspace.SHARED_WITH_ME) {
                viewPager2.post {
                    viewPager2.currentItem = Constants.POSITION_SHARED_WITH_ME_FRAGMENT
                }
            }
        }
    }

    private val onShowMessage = Observer<String> { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private val onSharesByMeRetrieved = Observer<MutableList<Record>> {
        viewPagerAdapter.setSharesByMe(it)
        arguments?.let { args ->
            viewPagerAdapter.sharedByMeFragment?.uploadFilesToFolder(
                args.getParcelable(PARCELABLE_RECORD_KEY),
                args.getParcelableArrayList(PARCELABLE_FILES_KEY)
            )
        }
    }

    private val onSharesWithMeRetrieved = Observer<MutableList<Record>> {
        viewPagerAdapter.setSharesWithMe(it)
        arguments?.let { args ->
            viewPagerAdapter.sharedWithMeFragment?.uploadFilesToFolder(
                args.getParcelable(PARCELABLE_RECORD_KEY),
                args.getParcelableArrayList(PARCELABLE_FILES_KEY)
            )
            args.clear()
        }
    }

    private val onShareByMeFragmentReady = Observer<Void?> {
        viewPagerAdapter.sharedByMeFragment?.getRootShares()?.observe(this, onGetRootShares)
        viewPagerAdapter.sharedByMeFragment?.getOnRecordSelected()
            ?.observe(this, onRecordSelectedObserver)
    }

    private val onShareWithMeFragmentReady = Observer<Void?> {
        viewPagerAdapter.sharedWithMeFragment?.getRootShares()?.observe(this, onGetRootShares)
        viewPagerAdapter.sharedWithMeFragment?.getOnRecordSelected()
            ?.observe(this, onRecordSelectedObserver)
    }

    private val onGetRootShares = Observer<Void?> {
        viewModel.requestShares()
    }

    private val onRecordSelectedObserver = Observer<Record> {
        val workspace = when (viewPager2.currentItem) {
            Constants.POSITION_SHARED_BY_ME_FRAGMENT -> Workspace.SHARED_BY_ME
            else -> Workspace.SHARED_WITH_ME
        }
        onRecordSelectedEvent.value = Pair(workspace, it)
    }

    fun getOnRecordSelected(): MutableLiveData<Pair<Workspace, Record>> = onRecordSelectedEvent

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onShowMessage)
        viewModel.getOnSharesByMeRetrieved().observe(this, onSharesByMeRetrieved)
        viewModel.getOnSharesWithMeRetrieved().observe(this, onSharesWithMeRetrieved)
        viewPagerAdapter.getOnShareByMeFragmentReady().observe(this, onShareByMeFragmentReady)
        viewPagerAdapter.getOnShareWithMeFragmentReady().observe(this, onShareWithMeFragmentReady)
        viewPagerAdapter.sharedByMeFragment?.getRootShares()?.observe(this, onGetRootShares)
        viewPagerAdapter.sharedWithMeFragment?.getRootShares()?.observe(this, onGetRootShares)
        viewPagerAdapter.sharedByMeFragment?.getOnRecordSelected()
            ?.observe(this, onRecordSelectedObserver)
        viewPagerAdapter.sharedWithMeFragment?.getOnRecordSelected()
            ?.observe(this, onRecordSelectedObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getOnSharesByMeRetrieved().removeObserver(onSharesByMeRetrieved)
        viewModel.getOnSharesWithMeRetrieved().removeObserver(onSharesWithMeRetrieved)
        viewPagerAdapter.getOnShareByMeFragmentReady().removeObserver(onShareByMeFragmentReady)
        viewPagerAdapter.getOnShareWithMeFragmentReady().removeObserver(onShareWithMeFragmentReady)
        viewPagerAdapter.sharedByMeFragment?.getRootShares()?.removeObserver(onGetRootShares)
        viewPagerAdapter.sharedWithMeFragment?.getRootShares()?.removeObserver(onGetRootShares)
        viewPagerAdapter.sharedByMeFragment?.getOnRecordSelected()
            ?.removeObserver(onRecordSelectedObserver)
        viewPagerAdapter.sharedWithMeFragment?.getOnRecordSelected()
            ?.removeObserver(onRecordSelectedObserver)
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