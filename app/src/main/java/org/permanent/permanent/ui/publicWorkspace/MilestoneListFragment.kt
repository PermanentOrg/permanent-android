package org.permanent.permanent.ui.publicWorkspace

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.activity_main.*
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentMilestoneListBinding
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.PublicProfileViewModel

class MilestoneListFragment: PermanentBaseFragment(){
    private lateinit var viewModel: PublicProfileViewModel
    private lateinit var binding: FragmentMilestoneListBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMilestoneListBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        activity?.toolbar?.menu?.findItem(R.id.plusItem)?.isVisible = true

        activity?.toolbar?.setOnMenuItemClickListener {
            if (it.itemId == R.id.plusItem) {
                requireParentFragment().findNavController().navigate(R.id.action_milestonesListFragment_to_editMilestonesFragment)
            }
            true
        }

        return binding.root
    }

    override fun connectViewModelEvents() {
    }

    override fun disconnectViewModelEvents() {
    }
}

