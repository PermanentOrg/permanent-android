package org.permanent.permanent.ui.legacyPlanning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.compose.LegacyContactScreen
import org.permanent.permanent.viewmodels.LegacyContactViewModel
import org.permanent.permanent.viewmodels.LegacyStatusViewModel

class LegacyContactFragment : PermanentBaseFragment() {

    private var addEditLegacyContactFragment: AddEditLegacyContactFragment? = null
    private lateinit var viewModel: LegacyContactViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[LegacyContactViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    LegacyContactScreen(viewModel = viewModel) {
                        addEditLegacyContactFragment = AddEditLegacyContactFragment()
                        addEditLegacyContactFragment?.show(parentFragmentManager, addEditLegacyContactFragment?.tag)
                    }
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