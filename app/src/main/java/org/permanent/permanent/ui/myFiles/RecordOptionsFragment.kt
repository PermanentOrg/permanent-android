package org.permanent.permanent.ui.myFiles

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.dialog_title_text_two_buttons.view.*
import org.permanent.permanent.DevicePermissionsHelper
import org.permanent.permanent.R
import org.permanent.permanent.REQUEST_CODE_WRITE_STORAGE_PERMISSION
import org.permanent.permanent.databinding.FragmentRecordOptionsBinding
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.Share
import org.permanent.permanent.ui.MenuSharesAdapter
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.ui.Workspace
import org.permanent.permanent.viewmodels.RecordOptionsViewModel

const val SHOWN_IN_WHICH_WORKSPACE = "shown_in_which_workspace_key"
const val IS_SHOWN_IN_SHARED_WITH_ME = "is_shown_in_shared_with_me_key"
const val IS_SHOWN_IN_ROOT_FOLDER = "is_shown_in_root_folder_key"

class RecordOptionsFragment : PermanentBottomSheetFragment() {
    private lateinit var binding: FragmentRecordOptionsBinding
    private lateinit var viewModel: RecordOptionsViewModel
    private lateinit var sharesRecyclerView: RecyclerView
    private lateinit var sharesAdapter: MenuSharesAdapter
    private lateinit var record: Record
    private var downloadingAlert: AlertDialog? = null
    private val onFileDownloadRequest = MutableLiveData<Record>()
    private val onRecordDeleteRequest = MutableLiveData<Record>()
    private val onRecordUnshareRequest = MutableLiveData<Record>()
    private val onRecordRenameRequest = MutableLiveData<Record>()
    private val onRecordManageSharingRequest = MutableLiveData<Record>()
    private val onRecordRelocateRequest = MutableLiveData<Pair<Record, RelocationType>>()

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[RecordOptionsViewModel::class.java]
        binding = FragmentRecordOptionsBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
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

        initSharesRecyclerView(binding.rvShares)

        return binding.root
    }

    private fun initSharesRecyclerView(rvShares: RecyclerView) {
        sharesRecyclerView = rvShares
        sharesAdapter = MenuSharesAdapter()
        sharesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = sharesAdapter
        }
    }

    private val onSharesRetrieved = Observer<List<Share>> {
        sharesAdapter.set(it)
    }

    private val onRequestWritePermission = Observer<Void> {
        DevicePermissionsHelper().requestWriteStoragePermission(this)
    }

    private val onFileDownloadRequestObserver = Observer<Void> {
        dismiss()
        onFileDownloadRequest.value = record
    }

    private val onDeleteObserver = Observer<Void> {
        dismiss()
        onRecordDeleteRequest.value = record
    }

    private val onUnshareObserver = Observer<Void> {
        dismiss()
        onRecordUnshareRequest.value = record
    }

    private val onRenameObserver = Observer<Void> {
        dismiss()
        onRecordRenameRequest.value = record
    }

    private val onManageSharingObserver = Observer<Void> {
        dismiss()
        onRecordManageSharingRequest.value = record
    }

    private val onShareToAnotherAppObserver = Observer<String> { contentType ->
        viewModel.getUriForSharing()?.let {
            shareFile(it, contentType)
            dismiss()
        } ?: run {
            downloadingAlert = context?.let {
                AlertDialog.Builder(it)
                    .setTitle(getString(R.string.downloading_file_in_progress))
                    .setNegativeButton(
                        getString(R.string.cancel_button)
                    ) { _, _ -> viewModel.cancelDownload() }
                    .create()
            }
            downloadingAlert?.show()
            viewModel.downloadFileForSharing(this)
        }
    }

    private val onFileDownloadedForSharing = Observer<String> { contentType ->
        downloadingAlert?.cancel()
        viewModel.getUriForSharing()?.let { shareFile(it, contentType) }
        dismiss()
    }

    private val showSnackbarSuccess = Observer<String> { message ->
        downloadingAlert?.cancel()
        dialog?.window?.decorView?.let {
            val snackBar = Snackbar.make(it, message, Snackbar.LENGTH_LONG)
            val view: View = snackBar.view
            context?.let {
                view.setBackgroundColor(ContextCompat.getColor(it, R.color.paleGreen))
                snackBar.setTextColor(ContextCompat.getColor(it, R.color.green))
            }
            val snackbarTextTextView = view.findViewById(R.id.snackbar_text) as TextView
            snackbarTextTextView.setTypeface(snackbarTextTextView.typeface, Typeface.BOLD)
            snackBar.show()
        }
    }

    private val showSnackbar = Observer<String> { message ->
        downloadingAlert?.cancel()
        dialog?.window?.decorView?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
        }
    }

    private val onRelocateRequestObserver = Observer<RelocationType> {
        dismiss()
        onRecordRelocateRequest.value = Pair(record, it)
    }

    private val onShareLinkObserver = Observer<String> {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, it)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private val onPublishRequestObserver = Observer<Void> {
        val viewDialog: View = layoutInflater.inflate(R.layout.dialog_title_text_two_buttons, null)
        val alert = android.app.AlertDialog.Builder(context).setView(viewDialog).create()

        viewDialog.tvTitle.text =
            getString(R.string.dialog_record_publish_confirmation_title, record.displayName)
        viewDialog.tvText.text = getString(R.string.dialog_record_publish_confirmation_text)
        viewDialog.btnPositive.text = getString(R.string.button_publish)
        viewDialog.btnPositive.setOnClickListener {
            viewModel.publishRecord()
            alert.dismiss()
        }
        viewDialog.btnNegative.setOnClickListener {
            alert.dismiss()
        }
        alert.show()
    }

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

    private fun shareFile(sharingUri: Uri, mimeType: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_STREAM, sharingUri)
        intent.type = mimeType
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(intent, ""))
    }

    fun getOnFileDownloadRequest(): MutableLiveData<Record> = onFileDownloadRequest

    fun getOnRecordDeleteRequest(): MutableLiveData<Record> = onRecordDeleteRequest

    fun getOnRecordUnshareRequest(): MutableLiveData<Record> = onRecordUnshareRequest

    fun getOnRecordRenameRequest(): MutableLiveData<Record> = onRecordRenameRequest

    fun getOnRecordManageSharingRequest(): MutableLiveData<Record> =
        onRecordManageSharingRequest

    fun getOnRecordRelocateRequest(): MutableLiveData<Pair<Record, RelocationType>> =
        onRecordRelocateRequest

    override fun connectViewModelEvents() {
        viewModel.getOnSharesRetrieved().observe(this, onSharesRetrieved)
        viewModel.getOnRequestWritePermission().observe(this, onRequestWritePermission)
        viewModel.getOnFileDownloadRequest().observe(this, onFileDownloadRequestObserver)
        viewModel.getOnDeleteRequest().observe(this, onDeleteObserver)
        viewModel.getOnUnshareRequest().observe(this, onUnshareObserver)
        viewModel.getOnRenameRequest().observe(this, onRenameObserver)
        viewModel.getOnManageSharingRequest().observe(this, onManageSharingObserver)
        viewModel.getOnShareToAnotherAppRequest().observe(this, onShareToAnotherAppObserver)
        viewModel.getOnRelocateRequest().observe(this, onRelocateRequestObserver)
        viewModel.getOnShareLinkRequest().observe(this, onShareLinkObserver)
        viewModel.getOnPublishRequest().observe(this, onPublishRequestObserver)
        viewModel.getOnFileDownloadedForSharing().observe(this, onFileDownloadedForSharing)
        viewModel.getShowSnackbar().observe(this, showSnackbar)
        viewModel.getShowSnackbarSuccess().observe(this, showSnackbarSuccess)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnSharesRetrieved().removeObserver(onSharesRetrieved)
        viewModel.getOnRequestWritePermission().removeObserver(onRequestWritePermission)
        viewModel.getOnFileDownloadRequest().removeObserver(onFileDownloadRequestObserver)
        viewModel.getOnDeleteRequest().removeObserver(onDeleteObserver)
        viewModel.getOnRenameRequest().removeObserver(onRenameObserver)
        viewModel.getOnManageSharingRequest().removeObserver(onManageSharingObserver)
        viewModel.getOnShareToAnotherAppRequest().removeObserver(onShareToAnotherAppObserver)
        viewModel.getOnRelocateRequest().removeObserver(onRelocateRequestObserver)
        viewModel.getOnShareLinkRequest().removeObserver(onShareLinkObserver)
        viewModel.getOnPublishRequest().removeObserver(onPublishRequestObserver)
        viewModel.getOnFileDownloadedForSharing().removeObserver(onFileDownloadedForSharing)
        viewModel.getShowSnackbar().observe(this, showSnackbar)
        viewModel.getShowSnackbarSuccess().observe(this, showSnackbarSuccess)
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