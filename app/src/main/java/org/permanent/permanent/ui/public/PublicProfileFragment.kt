package org.permanent.permanent.ui.public

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentPublicProfileBinding
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.PublicProfileViewModel


class PublicProfileFragment : PermanentBaseFragment() {
    private lateinit var viewModel: PublicProfileViewModel
    private lateinit var binding: FragmentPublicProfileBinding

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
        return binding.root
    }

    private val onEditAboutRequest = Observer<Void> {
        requireParentFragment().findNavController().navigate(R.id.action_publicFragment_to_editAboutFragment)
    }

    private val onEditPersonInformationRequest = Observer<Void> {
        requireParentFragment().findNavController().navigate(R.id.action_publicFragment_to_editPersonInformationFragment)
    }

    private val onEditMilestonesRequest = Observer<Void> {
        requireParentFragment().findNavController().navigate(R.id.action_publicFragment_to_milestonesListFragment)
    }

    private val onEditOnlinePresenceRequest = Observer<Void> {
        requireParentFragment().findNavController().navigate(R.id.action_publicFragment_to_onlinePresenceListFragment)
    }

    override fun connectViewModelEvents() {
        viewModel.getOnEditAboutRequest().observe(this, onEditAboutRequest)
        viewModel.getOnEditPersonInformationRequest().observe(this, onEditPersonInformationRequest)
        viewModel.getOnEditMilestonesRequest().observe(this, onEditMilestonesRequest)
        viewModel.getOnEditMilestonesRequest().observe(this, onEditOnlinePresenceRequest)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnEditAboutRequest().removeObserver(onEditAboutRequest)
        viewModel.getOnEditPersonInformationRequest().removeObserver(onEditPersonInformationRequest)
        viewModel.getOnEditMilestonesRequest().removeObserver(onEditMilestonesRequest)
        viewModel.getOnEditMilestonesRequest().removeObserver(onEditOnlinePresenceRequest)
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