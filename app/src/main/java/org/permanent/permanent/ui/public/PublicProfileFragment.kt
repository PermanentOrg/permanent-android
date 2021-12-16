package org.permanent.permanent.ui.public

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentPublicProfileBinding
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.PublicProfileViewModel


class PublicProfileFragment : PermanentBaseFragment() {
    private lateinit var viewModel: PublicProfileViewModel
    private var _binding: FragmentPublicProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPublicProfileBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    private val onEditAboutRequest = Observer<Void> {
        findNavController().navigate(R.id.action_publicFragment_to_editAboutFragment)
    }

    private val onEditPersonInformationRequest = Observer<Void> {
        findNavController().navigate(R.id.action_publicFragment_to_editPersonInformationFragment)
    }

    private val onEditMilestonesRequest = Observer<Void> {
        findNavController().navigate(R.id.action_publicFragment_to_editMilestonesFragment)
    }

    override fun connectViewModelEvents() {
        viewModel.getOnEditAboutRequest().observe(this, onEditAboutRequest)
        viewModel.getOnEditPersonInformationRequest().observe(this, onEditPersonInformationRequest)
        viewModel.getOnEditMilestonesRequest().observe(this, onEditMilestonesRequest)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnEditAboutRequest().removeObserver(onEditAboutRequest)
        viewModel.getOnEditPersonInformationRequest().removeObserver(onEditPersonInformationRequest)
        viewModel.getOnEditMilestonesRequest().removeObserver(onEditMilestonesRequest)
    }
}