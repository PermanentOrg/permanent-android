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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.viewmodels.ChecklistViewModel
import org.permanent.permanent.viewmodels.SingleLiveEvent

class ChecklistBottomSheetFragment : PermanentBottomSheetFragment() {

    private val viewModel: ChecklistViewModel by viewModels()
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
                    ChecklistBottomSheetContent(viewModel = viewModel, onClose = {
                        hideChecklistButton.call()
                        dismiss()
                    })
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        dialog?.let { dialog ->
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val screenHeight = Resources.getSystem().displayMetrics.heightPixels
                val desiredHeight = (screenHeight * 0.80).toInt()

                it.layoutParams.height = desiredHeight
                it.requestLayout()

                BottomSheetBehavior.from(it).apply {
                    state = BottomSheetBehavior.STATE_EXPANDED
                    skipCollapsed = true
                }
            }
        }
    }

    fun getHideChecklistButton(): MutableLiveData<Void?> = hideChecklistButton

    override fun connectViewModelEvents() {
    }

    override fun disconnectViewModelEvents() {
    }
}