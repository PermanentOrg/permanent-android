package org.permanent.permanent.ui.public

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_main.*
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentOnlinePresenceListBinding
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.PublicProfileViewModel

class OnlinePresenceListFragment: PermanentBaseFragment(){
    private lateinit var viewModel: PublicProfileViewModel
    private lateinit var binding: FragmentOnlinePresenceListBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnlinePresenceListBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        activity?.toolbar?.menu?.findItem(R.id.settingsItem)?.isVisible = false
        activity?.toolbar?.menu?.findItem(R.id.plusItem)?.isVisible = true

        return binding.root
    }

    override fun connectViewModelEvents() {
        TODO("Not yet implemented")
    }

    override fun disconnectViewModelEvents() {
        TODO("Not yet implemented")
    }

}

