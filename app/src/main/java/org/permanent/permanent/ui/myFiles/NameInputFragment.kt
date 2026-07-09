package org.permanent.permanent.ui.myFiles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.permanent.permanent.models.NavigationFolderIdentifier
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.ui.myFiles.compose.NewFolderScreen
import org.permanent.permanent.ui.myFiles.compose.RenameScreen
import org.permanent.permanent.viewmodels.NewFolderViewModel
import org.permanent.permanent.viewmodels.RenameRecordViewModel

private const val MODE_KEY = "name_input_mode"
private const val RECORD_KEY = "name_input_record"

class NameInputFragment : PermanentBottomSheetFragment() {

    enum class Mode { RENAME, NEW_FOLDER }

    private var onCompletedCallback: (() -> Unit)? = null

    companion object {
        fun forRename(record: Record): NameInputFragment = NameInputFragment().apply {
            arguments = Bundle().apply {
                putString(MODE_KEY, Mode.RENAME.name)
                putParcelable(RECORD_KEY, record)
            }
        }

        fun forNewFolder(folderIdentifier: NavigationFolderIdentifier?): NameInputFragment =
            NameInputFragment().apply {
                arguments = Bundle().apply {
                    putString(MODE_KEY, Mode.NEW_FOLDER.name)
                    putParcelable(FOLDER_IDENTIFIER_KEY, folderIdentifier)
                }
            }
    }

    fun setOnCompletedCallback(callback: () -> Unit) {
        onCompletedCallback = callback
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

        val mode = Mode.valueOf(arguments?.getString(MODE_KEY) ?: Mode.NEW_FOLDER.name)

        setContent {
            MaterialTheme {
                when (mode) {
                    Mode.RENAME -> {
                        val record: Record? = arguments?.getParcelable(RECORD_KEY)
                        val viewModel = ViewModelProvider(this@NameInputFragment)[RenameRecordViewModel::class.java]
                        if (record != null) {
                            viewModel.setRecordName(record.displayName)
                            RenameScreen(
                                viewModel = viewModel,
                                record = record,
                                onCompleted = { onCompletedCallback?.invoke(); dismiss() },
                                onClose = { dismiss() }
                            )
                        }
                    }
                    Mode.NEW_FOLDER -> {
                        val folderIdentifier: NavigationFolderIdentifier? =
                            arguments?.getParcelable(FOLDER_IDENTIFIER_KEY)
                        val viewModel = ViewModelProvider(this@NameInputFragment)[NewFolderViewModel::class.java]
                        NewFolderScreen(
                            viewModel = viewModel,
                            folderIdentifier = folderIdentifier,
                            onCompleted = { onCompletedCallback?.invoke(); dismiss() },
                            onClose = { dismiss() }
                        )
                    }
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
