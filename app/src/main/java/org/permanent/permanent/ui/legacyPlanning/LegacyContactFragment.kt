package org.permanent.permanent.ui.legacyPlanning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.permanent.permanent.R
import org.permanent.permanent.network.models.LegacyContact
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.compose.LegacyContactScreen
import org.permanent.permanent.viewmodels.LegacyContactViewModel

class LegacyContactFragment : PermanentBaseFragment() {

    private var addEditLegacyContactFragment: AddEditLegacyContactFragment? = null
    private lateinit var viewModel: LegacyContactViewModel
    private var legacyContact: LegacyContact? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[LegacyContactViewModel::class.java]
        val lifecycleOwner = viewLifecycleOwner

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    LegacyContactScreen(viewModel = viewModel, openAddEditScreen = {
                        addEditLegacyContactFragment = AddEditLegacyContactFragment()
                        addEditLegacyContactFragment?.show(parentFragmentManager, addEditLegacyContactFragment?.tag)
                        addEditLegacyContactFragment?.setBundleArguments(legacyContact)
                        addEditLegacyContactFragment?.getOnLegacyContactUpdated()?.observe(lifecycleOwner, onLegacyContactUpdatedObserver)
                    }) {
                        findNavController().navigate(R.id.action_legacyContactFragment_to_statusFragment)
                    }
                }
            }
        }
    }

    private val onLegacyContactReadyObserver = Observer<LegacyContact?> {
        legacyContact = it
    }

    private val onLegacyContactUpdatedObserver = Observer<LegacyContact> {
        legacyContact = it
        viewModel.onLegacyContactUpdated(it)
    }


    override fun connectViewModelEvents() {
        viewModel.getOnLegacyContactReady().observe(this, onLegacyContactReadyObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnLegacyContactReady().removeObserver(onLegacyContactReadyObserver)
        addEditLegacyContactFragment?.getOnLegacyContactUpdated()?.removeObserver(onLegacyContactUpdatedObserver)
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