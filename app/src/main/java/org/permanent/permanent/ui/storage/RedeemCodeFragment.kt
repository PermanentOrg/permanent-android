package org.permanent.permanent.ui.storage

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
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

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    RedeemCodeScreen(viewModel)
                }
            }
        }
    }

    private val onGiftStorageSentObserver = Observer<Void?> {
        view?.let { thisView ->
            viewModel.getGiftGB().value?.let { giftGB ->
                viewModel.getEmails().value?.size?.let { emailSize ->
                    showSuccessSnackbar(thisView, giftGB, emailSize)
                }
            }
        }
        findNavController().navigateUp()
    }

    private fun showSuccessSnackbar(thisView: View, giftGB: Int, emailSize: Int) {
        val snackBar = Snackbar.make(
            thisView,
            getString(R.string.storage_successfully_gifted, giftGB * emailSize),
            Snackbar.LENGTH_LONG
        )
        val view: View = snackBar.view
        context?.let {
            view.setBackgroundColor(ContextCompat.getColor(it, R.color.deepGreen))
            snackBar.setTextColor(ContextCompat.getColor(it, R.color.paleGreen))
        }
        val snackbarTextTextView = view.findViewById(R.id.snackbar_text) as TextView
        snackbarTextTextView.setTypeface(snackbarTextTextView.typeface, Typeface.BOLD)
        snackBar.show()
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