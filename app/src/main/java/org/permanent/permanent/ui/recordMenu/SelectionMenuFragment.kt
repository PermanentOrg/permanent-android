package org.permanent.permanent.ui.recordMenu

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.permanent.permanent.R
import org.permanent.permanent.ui.ConfirmationDialogFragment
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.ui.myFiles.ModificationType
import org.permanent.permanent.ui.recordMenu.compose.SelectionMenuScreen
import org.permanent.permanent.viewmodels.RecordMenuItem
import org.permanent.permanent.viewmodels.SelectionMenuViewModel

class SelectionMenuFragment : PermanentBottomSheetFragment() {
    private val viewModel: SelectionMenuViewModel by viewModels()
    private val onModifyRequest = MutableLiveData<ModificationType>()
    private var pendingConfirmationItem: RecordMenuItem? = null

    fun setSelectedRecords(records: List<RecordUiModel>) {
        val bundle = Bundle()
        bundle.putParcelableArrayList(SELECTED_RECORDS_KEY, ArrayList(records))
        arguments = bundle
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        val selectedRecords = arguments?.getParcelableArrayList<RecordUiModel>(SELECTED_RECORDS_KEY)
        selectedRecords?.let { viewModel.initWithSelection(it) }

        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme {
                Surface(
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                ) {
                    SelectionMenuScreen(
                        viewModel = viewModel,
                        onItemClick = { item -> handleMenuClick(item) },
                        onClose = { dismiss() })
                }
            }
        }
    }

    private fun handleMenuClick(item: RecordMenuItem) {
        when (item) {
            RecordMenuItem.EditMetadata -> onModifyRequest.value = ModificationType.EDIT
            RecordMenuItem.Copy -> onModifyRequest.value = ModificationType.COPY
            RecordMenuItem.Move -> onModifyRequest.value = ModificationType.MOVE
            RecordMenuItem.Delete -> pendingConfirmationItem = item
            else -> {}
        }
        dismiss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)

        dialog.setOnShowListener { d ->
            val bottomSheet =
                (d as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                // Transparent background so Compose Surface shape is visible
                it.background = Color.TRANSPARENT.toDrawable()

                BottomSheetBehavior.from(it).apply {
                    isFitToContents = true
                    skipCollapsed = true
                    state = BottomSheetBehavior.STATE_EXPANDED
                    peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO
                }
            }
        }

        return dialog
    }

    override fun onStart() {
        super.onStart()

        val dialog = dialog ?: return
        val bottomSheet =
            dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                ?: return
        val behavior = BottomSheetBehavior.from(bottomSheet)

        bottomSheet.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        behavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO
        behavior.isFitToContents = true
        behavior.skipCollapsed = true

        bottomSheet.post {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        pendingConfirmationItem?.let { item ->
            showConfirmationBottomSheetFor(item)
            pendingConfirmationItem = null
        }
    }

    private fun showConfirmationBottomSheetFor(item: RecordMenuItem) {
        val message = getString(R.string.confirm_delete_message, getString(R.string.the_selected_items))
        val boldText = getString(R.string.the_selected_items)
        val confirmText = getString(R.string.delete)

        val sheet = ConfirmationDialogFragment.newInstance(
            message = message,
            boldText = boldText,
            confirmLabel = confirmText
        )

        sheet.onConfirm = {
            when (item) {
                RecordMenuItem.Delete -> onModifyRequest.value = ModificationType.DELETE
                else -> Unit
            }
        }

        sheet.show(parentFragmentManager, "confirmation_sheet")
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

    fun getOnSelectionModifyRequest(): MutableLiveData<ModificationType> = onModifyRequest

    companion object {
        const val SELECTED_RECORDS_KEY = "selected_records_key"
    }
}