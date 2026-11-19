package org.permanent.permanent.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.FragmentManager
import org.permanent.permanent.R
import org.permanent.permanent.models.DisplayDated
import org.permanent.permanent.ui.composeComponents.ComingSoonBottomSheet

class ComingSoonDialogFragment : PermanentBottomSheetFragment() {

    companion object {
        private const val MESSAGE_KEY = "message"
        private const val TAG = "coming_soon_sheet"

        fun show(fragmentManager: FragmentManager, message: String) {
            ComingSoonDialogFragment().apply {
                arguments = Bundle().apply { putString(MESSAGE_KEY, message) }
            }.show(fragmentManager, TAG)
        }

        // Shown instead of the date sheet: true when any item has a date the app can't edit yet.
        fun showForComplexDates(fragmentManager: FragmentManager, context: Context, dated: List<DisplayDated?>): Boolean {
            val complex = dated.count { it?.hasComplexDate == true }
            if (complex == 0) return false
            val message = if (dated.size == 1) context.getString(R.string.complex_date_edit_message)
            else context.resources.getQuantityString(R.plurals.complex_date_bulk_edit_message, complex, complex, dated.size)
            show(fragmentManager, message)
            return true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        val message = arguments?.getString(MESSAGE_KEY).orEmpty()
        setContent {
            MaterialTheme {
                ComingSoonBottomSheet(message = message, onDismiss = { dismiss() })
            }
        }
    }

    override fun connectViewModelEvents() {
    }

    override fun disconnectViewModelEvents() {
    }
}
