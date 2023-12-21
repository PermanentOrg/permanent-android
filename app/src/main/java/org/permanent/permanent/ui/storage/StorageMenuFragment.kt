package org.permanent.permanent.ui.storage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.permanent.permanent.R
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.storage.compose.StorageMenuScreen
import org.permanent.permanent.viewmodels.StorageMenuViewModel

class StorageMenuFragment : PermanentBaseFragment() {

    private lateinit var viewModel: StorageMenuViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        viewModel = ViewModelProvider(this)[StorageMenuViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    StorageMenuScreen(
                        viewModel,
                        onAddStorageClick = { findNavController().navigate(R.id.action_storageMenuFragment_to_addStorageFragment) },
                        onGiftStorageClick = { findNavController().navigate(R.id.action_storageMenuFragment_to_giftStorageFragment) },
                        onRedeemCodeClick = { }
                    )
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
        viewModel.updateUsedStorage()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }
}