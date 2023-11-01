package org.permanent.permanent.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import org.permanent.permanent.ui.activities.MainActivity
import org.permanent.permanent.ui.compose.gifting.GiftStorageScreen
import org.permanent.permanent.viewmodels.GiftStorageViewModel

class GiftStorageFragment : PermanentBaseFragment() {

    private lateinit var viewModel: GiftStorageViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        viewModel = ViewModelProvider(this)[GiftStorageViewModel::class.java]

        arguments?.getLong(MainActivity.SPACE_TOTAL_KEY)?.let {
            viewModel.setSpaceTotal(it)
        }
        arguments?.getLong(MainActivity.SPACE_LEFT_KEY)?.let {
            viewModel.setSpaceLeft(it)
        }
        arguments?.getInt(MainActivity.SPACE_USED_PERCENTAGE_KEY)?.let {
            viewModel.setSpaceUsedPercentage(it)
        }
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    GiftStorageScreen(viewModel)
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