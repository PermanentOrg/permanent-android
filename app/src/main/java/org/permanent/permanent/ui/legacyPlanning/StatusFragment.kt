package org.permanent.permanent.ui.legacyPlanning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.compose.StatusScreen
import org.permanent.permanent.viewmodels.LegacyStatusViewModel

class StatusFragment: PermanentBaseFragment()  {
    private lateinit var viewModel: LegacyStatusViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        viewModel = ViewModelProvider(this)[LegacyStatusViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    StatusScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun connectViewModelEvents() {
        TODO("Not yet implemented")
    }

    override fun disconnectViewModelEvents() {
        TODO("Not yet implemented")
    }
}