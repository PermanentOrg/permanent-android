package org.permanent.permanent.ui.public

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.permanent.permanent.databinding.FragmentAddSocialMediaBinding
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.PublicProfileViewModel

class AddSocialMediaFragment: PermanentBaseFragment(){
    private lateinit var viewModel: PublicProfileViewModel
        private lateinit var binding: FragmentAddSocialMediaBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddSocialMediaBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun connectViewModelEvents() {
    }

    override fun disconnectViewModelEvents() {
    }

}
