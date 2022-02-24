package org.permanent.permanent.ui.publicWorkspace

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_main.*
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentEditMilestoneBinding
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.PublicProfileViewModel

class PublicProfileEditMilestonesFragment: PermanentBaseFragment(){
    private lateinit var viewModel: PublicProfileViewModel
    private lateinit var binding: FragmentEditMilestoneBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditMilestoneBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        activity?.toolbar?.menu?.findItem(R.id.settingsItem)?.isVisible = false

        return binding.root
    }

    override fun connectViewModelEvents() {
    }

    override fun disconnectViewModelEvents() {
    }

}
