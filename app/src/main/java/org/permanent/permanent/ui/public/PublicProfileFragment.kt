package org.permanent.permanent.ui.public

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentPublicProfileBinding
import org.permanent.permanent.models.AccountEventAction
import org.permanent.permanent.models.Archive
import org.permanent.permanent.models.ProfileItem
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.public.PublicViewPagerAdapter.Companion.IS_VIEW_ONLY_MODE
import org.permanent.permanent.viewmodels.PublicProfileViewModel

class PublicProfileFragment : PermanentBaseFragment() {

    private lateinit var binding: FragmentPublicProfileBinding
    private lateinit var viewModel: PublicProfileViewModel
    private lateinit var milestonesRecyclerView: RecyclerView
    private lateinit var milestonesAdapter: MilestonesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[PublicProfileViewModel::class.java]
        viewModel.sendEvent(AccountEventAction.OPEN_ARCHIVE_PROFILE, data = mapOf("page" to "Archive Profile"))
        binding = FragmentPublicProfileBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        initMilestonesRecyclerView(binding.rvMilestones)
        val archive: Archive? = arguments?.getParcelable(PublicFragment.ARCHIVE)
        viewModel.setArchive(archive)
        arguments?.takeIf { it.containsKey(IS_VIEW_ONLY_MODE) }?.apply {
            val isViewOnlyMode = getBoolean(IS_VIEW_ONLY_MODE)
            if (isViewOnlyMode) viewModel.setIsViewOnlyMode()
        }

        return binding.root
    }

    private val onEditArchiveBasicInfoRequest = Observer<MutableList<ProfileItem>> {
        val bundle = bundleOf(PARCELABLE_PROFILE_ITEM_LIST_KEY to it)
        requireParentFragment().findNavController().navigate(R.id.action_publicFragment_to_editArchiveBasicInfoFragment, bundle)
    }

    private val onEditArchiveFullDetailsRequest = Observer<MutableList<ProfileItem>> {
        val bundle = bundleOf(PARCELABLE_PROFILE_ITEM_LIST_KEY to it)
        requireParentFragment().findNavController().navigate(R.id.action_publicFragment_to_editArchiveFullDetailsFragment, bundle)
    }

    private val onEditMilestonesRequest = Observer<Void?> {
        requireParentFragment().findNavController().navigate(R.id.action_publicFragment_to_milestoneListFragment)
    }

    private val onEditOnlinePresenceRequest = Observer<Void?> {
        requireParentFragment().findNavController().navigate(R.id.action_publicFragment_to_onlinePresenceListFragment)
    }

    private val onShowMessage = Observer<String?> { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private val onMilestonesRetrieved = Observer<MutableList<ProfileItem>> {
        milestonesAdapter.set(it)
    }

    private val onShowOnlinePresence = Observer<Boolean> { isMore ->
        binding.tvOnlinePresenceText.maxLines = if (isMore) MAX_LINES_NO_LIMIT else MAX_LINES_ONLINE_PRESENCE
    }

    private fun initMilestonesRecyclerView(rvMilestones: RecyclerView) {
        milestonesRecyclerView = rvMilestones
        milestonesAdapter = MilestonesAdapter(null)
        milestonesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = milestonesAdapter
        }
    }

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onShowMessage)
        viewModel.getOnMilestonesRetrieved().observe(this, onMilestonesRetrieved)
        viewModel.getOnShowOnlinePresence().observe(this, onShowOnlinePresence)
        viewModel.getOnEditArchiveBasicInfoRequest().observe(this, onEditArchiveBasicInfoRequest)
        viewModel.getOnEditArchiveFullDetailsRequest().observe(this, onEditArchiveFullDetailsRequest)
        viewModel.getOnEditMilestonesRequest().observe(this, onEditMilestonesRequest)
        viewModel.getOnEditOnlinePresenceRequest().observe(this, onEditOnlinePresenceRequest)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getOnMilestonesRetrieved().removeObserver(onMilestonesRetrieved)
        viewModel.getOnShowOnlinePresence().removeObserver(onShowOnlinePresence)
        viewModel.getOnEditArchiveBasicInfoRequest().removeObserver(onEditArchiveBasicInfoRequest)
        viewModel.getOnEditArchiveFullDetailsRequest().removeObserver(onEditArchiveFullDetailsRequest)
        viewModel.getOnEditMilestonesRequest().removeObserver(onEditMilestonesRequest)
        viewModel.getOnEditOnlinePresenceRequest().removeObserver(onEditOnlinePresenceRequest)
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
        viewModel.getProfileItems()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }

    companion object {
        const val MAX_LINES_ONLINE_PRESENCE = 3
        const val MAX_LINES_NO_LIMIT = 900
        const val PARCELABLE_PROFILE_ITEM_KEY = "parcelable_profile_items_key"
        const val PARCELABLE_PROFILE_ITEM_LIST_KEY = "parcelable_profile_item_list_key"
    }
}