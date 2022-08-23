package org.permanent.permanent.ui.archiveOnboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.permanent.permanent.databinding.FragmentArchiveOnboardingWelcomeBinding
import org.permanent.permanent.ui.PermanentBaseFragment

class WelcomeFragment : PermanentBaseFragment() {

    private lateinit var binding: FragmentArchiveOnboardingWelcomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentArchiveOnboardingWelcomeBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this

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