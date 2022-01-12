package org.permanent.permanent.ui.public

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
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
        activity?.toolbar?.menu?.findItem(R.id.plusItem)?.isVisible = true

        activity?.toolbar?.setOnMenuItemClickListener {
            if (it.itemId == R.id.plusItem) {
                requireParentFragment().findNavController().navigate(R.id.action_onlinePresenceListFragment_to_addSocialMediaFragment)
            }
            true
        }

        return binding.root
    }

    override fun connectViewModelEvents() {
    }

    override fun disconnectViewModelEvents() {
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
