package org.permanent.permanent.ui.recordMenu

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toDrawable
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import org.permanent.permanent.DevicePermissionsHelper
import org.permanent.permanent.R
import org.permanent.permanent.REQUEST_CODE_WRITE_STORAGE_PERMISSION
import org.permanent.permanent.databinding.DialogTitleTextTwoButtonsBinding
import org.permanent.permanent.models.AccountEventAction
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.ConfirmationDialogFragment
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.ui.Workspace
import org.permanent.permanent.ui.myFiles.ModificationType
import org.permanent.permanent.ui.myFiles.PARCELABLE_RECORD_KEY
import org.permanent.permanent.ui.recordMenu.compose.RecordMenuScreen
import org.permanent.permanent.ui.shareManagement.ShareManagementFragment
import org.permanent.permanent.viewmodels.RecordMenuItem
import org.permanent.permanent.viewmodels.RecordMenuViewModel

const val SHOWN_IN_WHICH_WORKSPACE = "shown_in_which_workspace_key"
const val IS_SHOWN_IN_SHARED_WITH_ME = "is_shown_in_shared_with_me_key"
const val IS_SHOWN_IN_ROOT_FOLDER = "is_shown_in_root_folder_key"

class RecordMenuFragment : PermanentBottomSheetFragment() {
    private lateinit var record: Record
    private val onFileDownloadRequest = MutableLiveData<Record>()
    private val onRecordLeaveShareRequest = MutableLiveData<Record>()
    private var shareManagementFragment: ShareManagementFragment? = null
    private val onRecordPublishRequest = MutableLiveData<Record>()
    private val onRecordRenameRequest = MutableLiveData<Record>()
    private val onRecordRelocateRequest = MutableLiveData<Pair<Record, ModificationType>>()
    private val onRecordDeleteRequest = MutableLiveData<Record>()
    private val viewModel: RecordMenuViewModel by viewModels()
    private var pendingConfirmationItem: RecordMenuItem? = null

    fun setBundleArguments(
        record: Record,
        workspace: Workspace,
        isShownInSharedWithMe: Boolean = false,
        isShownInRootFolder: Boolean = false
    ) {
        val bundle = Bundle()
        bundle.putParcelable(PARCELABLE_RECORD_KEY, record)
        bundle.putParcelable(SHOWN_IN_WHICH_WORKSPACE, workspace)
        bundle.putBoolean(IS_SHOWN_IN_SHARED_WITH_ME, isShownInSharedWithMe)
        bundle.putBoolean(IS_SHOWN_IN_ROOT_FOLDER, isShownInRootFolder)
        this.arguments = bundle
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        arguments?.getParcelable<Record?>(PARCELABLE_RECORD_KEY)?.let {
            record = it
            val shownInWorkspace =
                arguments?.getParcelable<Workspace?>(SHOWN_IN_WHICH_WORKSPACE)
            val isShownInSharedWithMe =
                arguments?.getBoolean(IS_SHOWN_IN_SHARED_WITH_ME)
            val isShownInRootFolder =
                arguments?.getBoolean(IS_SHOWN_IN_ROOT_FOLDER) ?: false

            if (shownInWorkspace != null && isShownInSharedWithMe != null) {
                viewModel.initWith(it, shownInWorkspace, isShownInSharedWithMe, isShownInRootFolder)
            }
        }

        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme {
                Surface(
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                ) {
                    RecordMenuScreen(
                        viewModel = viewModel,
                        onItemClick = { item -> handleMenuClick(item) },
                        onClose = { dismiss() }
                    )
                }
            }
        }
    }

    private fun handleMenuClick(item: RecordMenuItem) {
        when (item) {
            RecordMenuItem.Share -> {
                showShareManagementFragment()
                dismiss()
            }
            RecordMenuItem.Publish -> {
                showConfirmationDialogForPublish()
                dismiss()
            }
            RecordMenuItem.Rename -> {
                onRecordRenameRequest.value = record
                dismiss()
            }
            RecordMenuItem.Move -> {
                onRecordRelocateRequest.value = Pair(record, ModificationType.MOVE)
                dismiss()
            }
            RecordMenuItem.Copy -> {
                onRecordRelocateRequest.value = Pair(record, ModificationType.COPY)
                dismiss()
            }
            RecordMenuItem.Delete,
            RecordMenuItem.LeaveShare -> {
                pendingConfirmationItem = item
                dismiss()
            }
            RecordMenuItem.SendACopy -> viewModel.onSendACopyClick()
            RecordMenuItem.Download -> {
                viewModel.onDownloadClick()
                dismiss()
            }
            else -> {}
        }
    }

    override fun onStart() {
        super.onStart()

        val dialog = dialog ?: return
        val bottomSheet =
            dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                ?: return
        val behavior = BottomSheetBehavior.from(bottomSheet)

        // Allow dynamic height
        bottomSheet.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        behavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO
        behavior.isFitToContents = true
        behavior.skipCollapsed = true

        // Expand naturally
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
        val message = viewModel.getConfirmationMessageFor(item)
        val boldText = viewModel.getConfirmationBoldTextFor(item)
        val confirmText = viewModel.getConfirmationButtonLabelFor(item)

        val sheet = ConfirmationDialogFragment.newInstance(
            message = message,
            boldText = boldText,
            confirmLabel = confirmText
        )

        sheet.onConfirm = {
            when (item) {
                RecordMenuItem.Delete -> onRecordDeleteRequest.value = record
                RecordMenuItem.LeaveShare -> onRecordLeaveShareRequest.value = record
                else -> Unit
            }
        }

        sheet.show(parentFragmentManager, "confirmation_sheet")
    }

    private fun showShareManagementFragment() {
        shareManagementFragment = ShareManagementFragment()
        shareManagementFragment?.setBundleArguments(record, viewModel.getShareByUrlVO())
        shareManagementFragment?.show(parentFragmentManager, shareManagementFragment?.tag)
    }

    private fun showConfirmationDialogForPublish() {
        val dialogBinding: DialogTitleTextTwoButtonsBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.dialog_title_text_two_buttons, null, false
        )
        val alert = android.app.AlertDialog.Builder(context).setView(dialogBinding.root).create()

        dialogBinding.tvTitle.text =
            getString(R.string.dialog_record_publish_confirmation_title, record.displayName)
        dialogBinding.tvText.text = getString(R.string.dialog_record_publish_confirmation_text)
        dialogBinding.btnPositive.text = getString(R.string.button_publish)
        dialogBinding.btnPositive.setOnClickListener {
            onRecordPublishRequest.value = record
            alert.dismiss()
        }
        dialogBinding.btnNegative.setOnClickListener {
            alert.dismiss()
        }
        alert.show()
    }

    private val showSnackbar = Observer<String> { message ->
        dialog?.window?.decorView?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
        }
    }

    private val onShareToAnotherAppObserver = Observer<String> { contentType ->
        viewModel.getUriForSharing()?.let {
            shareFile(it, contentType)
            dismiss()
        } ?: run {
            viewModel.downloadFileForSharing(this)
        }

        viewModel.sendEvent(AccountEventAction.OPEN_SHARE_MODAL)
    }

    private fun shareFile(sharingUri: Uri, mimeType: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_STREAM, sharingUri)
        intent.type = mimeType
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(intent, ""))
    }

    private val onFileDownloadedForSharing = Observer<String> { contentType ->
        viewModel.getUriForSharing()?.let { shareFile(it, contentType) }
        dismiss()
    }

    private val onRequestWritePermission = Observer<Void?> {
        DevicePermissionsHelper().requestWriteStoragePermission(this)
    }

    private val onFileDownloadRequestObserver = Observer<Void?> {
        onFileDownloadRequest.value = record
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_WRITE_STORAGE_PERMISSION ->
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    viewModel.onWritePermissionGranted()
                } else {
                    Toast.makeText(
                        context, R.string.download_no_permissions_error, Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    fun getOnRecordPublishRequest(): MutableLiveData<Record> = onRecordPublishRequest
    fun getOnFileDownloadRequest(): MutableLiveData<Record> = onFileDownloadRequest
    fun getOnRecordRenameRequest(): MutableLiveData<Record> = onRecordRenameRequest

    fun getOnRecordRelocateRequest(): MutableLiveData<Pair<Record, ModificationType>> =
        onRecordRelocateRequest

    fun getOnRecordDeleteRequest(): MutableLiveData<Record> = onRecordDeleteRequest

    fun getOnRecordLeaveShareRequest(): MutableLiveData<Record> = onRecordLeaveShareRequest

    override fun connectViewModelEvents() {
        viewModel.getShowSnackbar().observe(this, showSnackbar)
        viewModel.getOnShareToAnotherAppRequest().observe(this, onShareToAnotherAppObserver)
        viewModel.getOnFileDownloadedForSharing().observe(this, onFileDownloadedForSharing)
        viewModel.getOnRequestWritePermission().observe(this, onRequestWritePermission)
        viewModel.getOnFileDownloadRequest().observe(this, onFileDownloadRequestObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowSnackbar().observe(this, showSnackbar)
        viewModel.getOnShareToAnotherAppRequest().removeObserver(onShareToAnotherAppObserver)
        viewModel.getOnFileDownloadedForSharing().removeObserver(onFileDownloadedForSharing)
        viewModel.getOnRequestWritePermission().removeObserver(onRequestWritePermission)
        viewModel.getOnFileDownloadRequest().removeObserver(onFileDownloadRequestObserver)
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