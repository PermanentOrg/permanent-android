package org.permanent.permanent.ui.storage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.activities.MainActivity
import org.permanent.permanent.ui.storage.compose.StorageMenuScreen
import org.permanent.permanent.viewmodels.StorageMenuViewModel

class StorageMenuFragment : PermanentBaseFragment() {

    private lateinit var viewModel: StorageMenuViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        viewModel = ViewModelProvider(this)[StorageMenuViewModel::class.java]

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
                    StorageMenuScreen(viewModel)
                }
            }
        }
    }

    private val onGiftStorageSentObserver = Observer<Void?> {
        view?.let { thisView ->
            viewModel.getGiftGB().value?.let { giftGB ->
                viewModel.getEmails().value?.size?.let { emailSize ->
//                    showSuccessSnackbar(thisView, giftGB, emailSize)
                }
            }
        }
        findNavController().navigateUp()
    }

    override fun connectViewModelEvents() {
        viewModel.getOnGiftStorageSent().observe(this, onGiftStorageSentObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnGiftStorageSent().removeObserver(onGiftStorageSentObserver)
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