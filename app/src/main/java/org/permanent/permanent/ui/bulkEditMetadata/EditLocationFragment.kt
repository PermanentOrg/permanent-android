package org.permanent.permanent.ui.bulkEditMetadata

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.ui.bulkEditMetadata.compose.EditLocationScreen
import org.permanent.permanent.ui.myFiles.PARCELABLE_FILES_KEY
import org.permanent.permanent.viewmodels.EditFileNamesViewModel

class EditLocationFragment : PermanentBottomSheetFragment() {
    private lateinit var viewModel: EditFileNamesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[EditFileNamesViewModel::class.java]

        arguments?.getParcelableArrayList<Record>(PARCELABLE_FILES_KEY)?.let {
            viewModel.setRecords(it)
        }

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    EditLocationScreen(cancel = {
                        this@EditLocationFragment.dismiss()
                    })
                }
            }
        }
    }

    fun setBundleArguments(records: ArrayList<Record>) {
        val bundle = Bundle()
        bundle.putParcelableArrayList(PARCELABLE_FILES_KEY, records)
        this.arguments = bundle
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener { dialog: DialogInterface ->
            val sheetDialog = dialog as BottomSheetDialog
            val bottomSheet =
                sheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from(bottomSheet as FrameLayout)
                .setState(BottomSheetBehavior.STATE_EXPANDED)
        }
        return bottomSheetDialog
    }

    override fun connectViewModelEvents() {

    }

    override fun disconnectViewModelEvents() {

    }
}