package org.permanent.permanent.ui.public

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.permanent.permanent.databinding.FragmentPublicGalleryBinding
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.PublicProfileViewModel

class PublicGalleryFragment: PermanentBaseFragment() {
    private lateinit var viewModel: PublicProfileViewModel
    private lateinit var binding: FragmentPublicGalleryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPublicGalleryBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun connectViewModelEvents() {
    }

    override fun disconnectViewModelEvents() {
    }
}