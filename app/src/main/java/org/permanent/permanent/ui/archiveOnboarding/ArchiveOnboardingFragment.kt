package org.permanent.permanent.ui.archiveOnboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import org.permanent.permanent.databinding.FragmentArchiveOnboardingTypeSelectionBinding
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.archiveOnboarding.compose.ArchiveOnboardingScreen
import org.permanent.permanent.viewmodels.ArchiveOnboardingViewModel

class ArchiveOnboardingFragment : PermanentBaseFragment() {

    private lateinit var viewModel: ArchiveOnboardingViewModel
    private lateinit var binding: FragmentArchiveOnboardingTypeSelectionBinding
    private lateinit var prefsHelper: PreferencesHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[ArchiveOnboardingViewModel::class.java]
        binding = FragmentArchiveOnboardingTypeSelectionBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        prefsHelper = PreferencesHelper(
            requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        )

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    ArchiveOnboardingScreen(viewModel)
                }
            }
        }
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