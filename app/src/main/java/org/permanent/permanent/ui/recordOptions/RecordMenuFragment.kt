package org.permanent.permanent.ui.recordOptions

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
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
import org.permanent.permanent.R
import org.permanent.permanent.databinding.DialogTitleTextTwoButtonsBinding
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.ui.Workspace
import org.permanent.permanent.ui.myFiles.IS_SHOWN_IN_ROOT_FOLDER
import org.permanent.permanent.ui.myFiles.IS_SHOWN_IN_SHARED_WITH_ME
import org.permanent.permanent.ui.myFiles.ModificationType
import org.permanent.permanent.ui.myFiles.PARCELABLE_RECORD_KEY
import org.permanent.permanent.ui.myFiles.SHOWN_IN_WHICH_WORKSPACE
import org.permanent.permanent.ui.recordOptions.compose.RecordMenuScreen
import org.permanent.permanent.ui.shareManagement.ShareManagementFragment
import org.permanent.permanent.viewmodels.RecordMenuViewModel

class RecordMenuFragment : PermanentBottomSheetFragment() {
    private lateinit var record: Record
    private var downloadingAlert: AlertDialog? = null
    private val onFileDownloadRequest = MutableLiveData<Record>()
    private val onRecordLeaveShareRequest = MutableLiveData<Record>()
    private var shareManagementFragment: ShareManagementFragment? = null
    private val onRecordPublishRequest = MutableLiveData<Record>()
    private val onRecordRenameRequest = MutableLiveData<Record>()
    private val onRecordRelocateRequest = MutableLiveData<Pair<Record, ModificationType>>()
    private val onRecordDeleteRequest = MutableLiveData<Record>()
    private val viewModel: RecordMenuViewModel by viewModels()

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
                        onItemClick = { item ->
                            viewModel.onMenuItemClick(item)
                            dismiss()
                        },
                        onClose = { dismiss() }
                    )
                }
            }
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

//    private val onRequestWritePermission = Observer<Void?> {
//        DevicePermissionsHelper().requestWriteStoragePermission(this)
//    }
//
//    private val onFileDownloadRequestObserver = Observer<Void?> {
//        dismiss()
//        onFileDownloadRequest.value = record
//    }
//
//    private val onShareToAnotherAppObserver = Observer<String> { contentType ->
//        viewModel.getUriForSharing()?.let {
//            shareFile(it, contentType)
//            dismiss()
//        } ?: run {
//            downloadingAlert = context?.let {
//                AlertDialog.Builder(it)
//                    .setTitle(getString(R.string.downloading_file_in_progress))
//                    .setNegativeButton(
//                        getString(R.string.button_cancel)
//                    ) { _, _ -> viewModel.cancelDownload() }
//                    .create()
//            }
//            downloadingAlert?.show()
//            viewModel.downloadFileForSharing(this)
//        }
//
//        viewModel.sendEvent(AccountEventAction.OPEN_SHARE_MODAL)
//    }
//
//    private val onFileDownloadedForSharing = Observer<String> { contentType ->
//        downloadingAlert?.cancel()
//        viewModel.getUriForSharing()?.let { shareFile(it, contentType) }
//        dismiss()
//    }
//
//    private val showSnackbarSuccess = Observer<String> { message ->
//        downloadingAlert?.cancel()
//        dialog?.window?.decorView?.let {
//            val snackBar = Snackbar.make(it, message, Snackbar.LENGTH_LONG)
//            val view: View = snackBar.view
//            context?.let {
//                view.setBackgroundColor(ContextCompat.getColor(it, R.color.deepGreen))
//                snackBar.setTextColor(ContextCompat.getColor(it, R.color.paleGreen))
//            }
//            val snackbarTextTextView = view.findViewById(R.id.snackbar_text) as TextView
//            snackbarTextTextView.setTypeface(snackbarTextTextView.typeface, Typeface.BOLD)
//            snackBar.show()
//        }
//    }
//
//    private val showSnackbar = Observer<String> { message ->
//        downloadingAlert?.cancel()
//        dialog?.window?.decorView?.let {
//            Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
//        }
//    }
//
//    private val onShareLinkObserver = Observer<String> {
//        val sendIntent: Intent = Intent().apply {
//            action = Intent.ACTION_SEND
//            putExtra(Intent.EXTRA_TEXT, it)
//            type = "text/plain"
//        }
//
//        val shareIntent = Intent.createChooser(sendIntent, null)
//        startActivity(shareIntent)
//    }
//    private val onLeaveShareObserver = Observer<Void?> {
//        dismiss()
//        onRecordLeaveShareRequest.value = record
//    }
//
//    private val onManageSharingObserver = Observer<Void?> {
//        shareManagementFragment = ShareManagementFragment()
//        shareManagementFragment?.setBundleArguments(record, viewModel.getShareByUrlVO())
//        shareManagementFragment?.show(parentFragmentManager, shareManagementFragment?.tag)
//        dismiss()
//    }
//
    //    @Deprecated("Deprecated in Java")
//    override fun onRequestPermissionsResult(
//        requestCode: Int, permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        when (requestCode) {
//            REQUEST_CODE_WRITE_STORAGE_PERMISSION ->
//                if (grantResults.isNotEmpty()
//                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
//                ) {
//                    viewModel.onWritePermissionGranted()
//                } else {
//                    Toast.makeText(
//                        context, R.string.download_no_permissions_error, Toast.LENGTH_LONG
//                    ).show()
//                }
//        }
//    }
//
//    private fun shareFile(sharingUri: Uri, mimeType: String) {
//        val intent = Intent(Intent.ACTION_SEND)
//        intent.putExtra(Intent.EXTRA_STREAM, sharingUri)
//        intent.type = mimeType
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//        startActivity(Intent.createChooser(intent, ""))
//    }
//
//    fun getOnFileDownloadRequest(): MutableLiveData<Record> = onFileDownloadRequest
//
//    fun getOnRecordLeaveShareRequest(): MutableLiveData<Record> = onRecordLeaveShareRequest

    private val onPublishRequestObserver = Observer<Void?> {
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

    private val onRenameObserver = Observer<Void?> {
        onRecordRenameRequest.value = record
    }

    private val onRelocateRequestObserver = Observer<ModificationType> {
        onRecordRelocateRequest.value = Pair(record, it)
    }

    private val onDeleteObserver = Observer<Void?> {
        onRecordDeleteRequest.value = record
    }

    fun getOnRecordPublishRequest(): MutableLiveData<Record> = onRecordPublishRequest
    fun getOnRecordRenameRequest(): MutableLiveData<Record> = onRecordRenameRequest

    fun getOnRecordRelocateRequest(): MutableLiveData<Pair<Record, ModificationType>> =
        onRecordRelocateRequest

    fun getOnRecordDeleteRequest(): MutableLiveData<Record> = onRecordDeleteRequest

    override fun connectViewModelEvents() {
        //        viewModel.getShowSnackbar().observe(this, showSnackbar)
//        viewModel.getShowSnackbarSuccess().observe(this, showSnackbarSuccess)
//        viewModel.getOnRequestWritePermission().observe(this, onRequestWritePermission)
//        viewModel.getOnFileDownloadRequest().observe(this, onFileDownloadRequestObserver)
//        viewModel.getOnLeaveShareRequest().observe(this, onLeaveShareObserver)
//        viewModel.getOnManageSharingRequest().observe(this, onManageSharingObserver)
//        viewModel.getOnShareToAnotherAppRequest().observe(this, onShareToAnotherAppObserver)
//        viewModel.getOnShareLinkRequest().observe(this, onShareLinkObserver)
//        viewModel.getOnFileDownloadedForSharing().observe(this, onFileDownloadedForSharing)
        viewModel.getOnPublishRequest().observe(this, onPublishRequestObserver)
        viewModel.getOnRenameRequest().observe(this, onRenameObserver)
        viewModel.getOnRelocateRequest().observe(this, onRelocateRequestObserver)
        viewModel.getOnDeleteRequest().observe(this, onDeleteObserver)
    }

    override fun disconnectViewModelEvents() {
//        viewModel.getShowSnackbar().observe(this, showSnackbar)
//        viewModel.getShowSnackbarSuccess().observe(this, showSnackbarSuccess)
//        viewModel.getOnRequestWritePermission().removeObserver(onRequestWritePermission)
//        viewModel.getOnFileDownloadRequest().removeObserver(onFileDownloadRequestObserver)
//        viewModel.getOnManageSharingRequest().removeObserver(onManageSharingObserver)
//        viewModel.getOnShareToAnotherAppRequest().removeObserver(onShareToAnotherAppObserver)
//        viewModel.getOnShareLinkRequest().removeObserver(onShareLinkObserver)
//        viewModel.getOnFileDownloadedForSharing().removeObserver(onFileDownloadedForSharing)
        viewModel.getOnPublishRequest().removeObserver(onPublishRequestObserver)
        viewModel.getOnRenameRequest().removeObserver(onRenameObserver)
        viewModel.getOnRelocateRequest().removeObserver(onRelocateRequestObserver)
        viewModel.getOnDeleteRequest().removeObserver(onDeleteObserver)
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