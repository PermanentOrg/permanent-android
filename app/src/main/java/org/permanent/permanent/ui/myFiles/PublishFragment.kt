package org.permanent.permanent.ui.myFiles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.ui.myFiles.compose.PublishScreen

private const val PUBLISH_RECORD_KEY = "publish_record_key"

class PublishFragment : PermanentBottomSheetFragment() {

    private var onPublishConfirmedCallback: (() -> Unit)? = null

    companion object {
        fun forRecord(record: Record): PublishFragment = PublishFragment().apply {
            arguments = Bundle().apply {
                putParcelable(PUBLISH_RECORD_KEY, record)
            }
        }
    }

    fun setOnPublishConfirmedCallback(callback: () -> Unit) {
        onPublishConfirmedCallback = callback
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheet?.let {
                it.setBackgroundResource(android.R.color.transparent)
                val behavior = BottomSheetBehavior.from(it)
                behavior.skipCollapsed = true
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

        val record: Record? = arguments?.getParcelable(PUBLISH_RECORD_KEY)

        setContent {
            MaterialTheme {
                if (record != null) {
                    PublishScreen(
                        record = record,
                        onPublish = { onPublishConfirmedCallback?.invoke(); dismiss() },
                        onClose = { dismiss() }
                    )
                }
            }
        }
    }

    override fun connectViewModelEvents() {}

    override fun disconnectViewModelEvents() {}

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }
}
