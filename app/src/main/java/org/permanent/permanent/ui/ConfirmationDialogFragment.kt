package org.permanent.permanent.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import org.permanent.permanent.ui.composeComponents.ConfirmationBottomSheet

class ConfirmationDialogFragment : PermanentBottomSheetFragment() {

    var onConfirm: (() -> Unit)? = null

    companion object {
        fun newInstance(message: String, boldText: String, confirmLabel: String): ConfirmationDialogFragment {
            return ConfirmationDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("message", message)
                    putString("boldText", boldText)
                    putString("confirmLabel", confirmLabel)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        val message = arguments?.getString("message") ?: ""
        val boldText = arguments?.getString("boldText") ?: ""
        val confirmLabel = arguments?.getString("confirmLabel") ?: ""

        setContent {
            MaterialTheme {
                ConfirmationBottomSheet(
                    message = message,
                    boldText = boldText,
                    confirmationButtonText = confirmLabel,
                    onConfirm = {
                        onConfirm?.invoke()
                        dismiss()
                    },
                    onDismiss = { dismiss() }
                )
            }
        }
    }

    override fun connectViewModelEvents() {
    }

    override fun disconnectViewModelEvents() {
    }
}