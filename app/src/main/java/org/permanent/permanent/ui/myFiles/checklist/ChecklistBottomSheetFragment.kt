package org.permanent.permanent.ui.myFiles.checklist

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.permanent.permanent.network.models.ChecklistItem
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.viewmodels.ChecklistViewModel
import org.permanent.permanent.viewmodels.SingleLiveEvent

class ChecklistBottomSheetFragment : PermanentBottomSheetFragment() {

    private val viewModel: ChecklistViewModel by viewModels()
    private val onChecklistItemClick = SingleLiveEvent<ChecklistItem>()
    private val hideChecklistButton = SingleLiveEvent<Void?>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            setContent {
                MaterialTheme {
                    ChecklistBottomSheetContent(viewModel = viewModel,
                        onItemClick = {
                            onChecklistItemClick.value = it
                            dismiss()
                        },
                        onClose = {
                            dismiss()
                        }, onHideChecklistButton = {
                            hideChecklistButton.call()
                            dismiss()
                        }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val dialog = dialog ?: return
        val bottomSheet = dialog.findViewById<View>(R.id.design_bottom_sheet) ?: return
        val window = dialog.window ?: return

        BottomSheetBehavior.from(bottomSheet).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }

        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val isTablet = viewModel.isTablet()

        val targetWidth = if (isTablet) screenWidth / 2 else ViewGroup.LayoutParams.MATCH_PARENT

        window.setLayout(targetWidth, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    fun getOnChecklistItemClick(): MutableLiveData<ChecklistItem> = onChecklistItemClick

    fun getHideChecklistButton(): MutableLiveData<Void?> = hideChecklistButton

    override fun connectViewModelEvents() {
    }

    override fun disconnectViewModelEvents() {
    }
}