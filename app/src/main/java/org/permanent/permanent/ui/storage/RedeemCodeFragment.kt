package org.permanent.permanent.ui.storage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.permanent.permanent.R
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.storage.compose.RedeemCodeScreen
import org.permanent.permanent.viewmodels.RedeemCodeViewModel

class RedeemCodeFragment : PermanentBaseFragment() {

    private lateinit var viewModel: RedeemCodeViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        viewModel = ViewModelProvider(this)[RedeemCodeViewModel::class.java]

        arguments?.getString(DEEPLINK_PROMO_CODE_KEY)?.let { viewModel.updateEnteredCode(it) }

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    RedeemCodeScreen(viewModel)
                }
            }
        }
    }

    private val onGiftStorageRedeemedObserver = Observer<Int> {
        val bundle = bundleOf(PROMO_SIZE_IN_MB_KEY to it)
        findNavController().navigate(R.id.action_redeemCodeFragment_to_storageMenuFragment, bundle)
    }

    override fun connectViewModelEvents() {
        viewModel.getOnGiftRedeemed().observe(this, onGiftStorageRedeemedObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnGiftRedeemed().removeObserver(onGiftStorageRedeemedObserver)
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }

    companion object {
        const val PROMO_SIZE_IN_MB_KEY = "promo_size_in_mb"
        const val DEEPLINK_PROMO_CODE_KEY = "promo_code"
    }
}