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
import org.permanent.permanent.models.ProfileItem
import org.permanent.permanent.ui.PermanentBaseFragment
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
        viewModel = ViewModelProvider(this).get(PublicProfileViewModel::class.java)
        binding = FragmentPublicProfileBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        initMilestonesRecyclerView(binding.rvMilestones)

        return binding.root
    }

    private val onEditAboutRequest = Observer<MutableList<ProfileItem>> {
        val bundle = bundleOf(PARCELABLE_PROFILE_ITEM_LIST_KEY to it)
        requireParentFragment().findNavController().navigate(R.id.action_publicFragment_to_editAboutFragment, bundle)
    }

    private val onEditArchiveInformationRequest = Observer<MutableList<ProfileItem>> {
        val bundle = bundleOf(PARCELABLE_PROFILE_ITEM_LIST_KEY to it)
        requireParentFragment().findNavController().navigate(R.id.action_publicFragment_to_editArchiveInformationFragment, bundle)
    }

    private val onEditMilestonesRequest = Observer<Void> {
        requireParentFragment().findNavController().navigate(R.id.action_publicFragment_to_milestoneListFragment)
    }

    private val onEditOnlinePresenceRequest = Observer<Void> {
        requireParentFragment().findNavController().navigate(R.id.action_publicFragment_to_onlinePresenceListFragment)
    }

    private val onShowMessage = Observer<String?> { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private val onMilestonesRetrieved = Observer<MutableList<ProfileItem>> {
        milestonesAdapter.set(it)
    }

    private val onReadAbout = Observer<Boolean> { isMore ->
        binding.tvAboutText.maxLines = if (isMore) MAX_LINES_NO_LIMIT else MAX_LINES_ABOUT
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
        viewModel.getOnReadAbout().observe(this, onReadAbout)
        viewModel.getOnShowOnlinePresence().observe(this, onShowOnlinePresence)
        viewModel.getOnEditAboutRequest().observe(this, onEditAboutRequest)
        viewModel.getOnEditArchiveInformationRequest().observe(this, onEditArchiveInformationRequest)
        viewModel.getOnEditMilestonesRequest().observe(this, onEditMilestonesRequest)
        viewModel.getOnEditOnlinePresenceRequest().observe(this, onEditOnlinePresenceRequest)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getOnMilestonesRetrieved().removeObserver(onMilestonesRetrieved)
        viewModel.getOnReadAbout().removeObserver(onReadAbout)
        viewModel.getOnShowOnlinePresence().removeObserver(onShowOnlinePresence)
        viewModel.getOnEditAboutRequest().removeObserver(onEditAboutRequest)
        viewModel.getOnEditArchiveInformationRequest().removeObserver(onEditArchiveInformationRequest)
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
        const val MAX_LINES_ABOUT = 5
        const val MAX_LINES_ONLINE_PRESENCE = 3
        const val MAX_LINES_NO_LIMIT = 900
        const val PARCELABLE_PROFILE_ITEM_KEY = "parcelable_profile_items_key"
        const val PARCELABLE_PROFILE_ITEM_LIST_KEY = "parcelable_profile_item_list_key"
    }
}