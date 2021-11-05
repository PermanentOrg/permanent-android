package org.permanent.permanent.ui.myFiles

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import org.permanent.permanent.PermissionsHelper
import org.permanent.permanent.R
import org.permanent.permanent.REQUEST_CODE_WRITE_STORAGE_PERMISSION
import org.permanent.permanent.databinding.FragmentRecordOptionsBinding
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.viewmodels.RecordOptionsViewModel

const val IS_SHOWN_IN_MY_FILES_KEY = "is_shown_in_my_files_key"

class RecordOptionsFragment : PermanentBottomSheetFragment() {
    private lateinit var binding: FragmentRecordOptionsBinding
    private lateinit var viewModel: RecordOptionsViewModel
    private var record: Record? = null
    private var downloadingAlert: AlertDialog? = null
    private val onFileDownloadRequest = MutableLiveData<Record>()
    private val onRecordDeleteRequest = MutableLiveData<Record>()
    private val onRecordRenameRequest = MutableLiveData<Record>()
    private val onRecordShareViaPermanentRequest = MutableLiveData<Record>()
    private val onRecordRelocateRequest = MutableLiveData<Pair<Record, RelocationType>>()

    fun setBundleArguments(record: Record, isShownInMyFilesFragment: Boolean) {
        val bundle = Bundle()
        bundle.putParcelable(PARCELABLE_RECORD_KEY, record)
        bundle.putBoolean(IS_SHOWN_IN_MY_FILES_KEY, isShownInMyFilesFragment)
        this.arguments = bundle
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(RecordOptionsViewModel::class.java)
        binding = FragmentRecordOptionsBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        record = arguments?.getParcelable(PARCELABLE_RECORD_KEY)
        viewModel.setRecord(record, arguments?.getBoolean(IS_SHOWN_IN_MY_FILES_KEY))
        return binding.root
    }

    private val onRequestWritePermission = Observer<Void> {
        PermissionsHelper().requestWriteStoragePermission(this)
    }

    private val onFileDownloadRequestObserver = Observer<Void> {
        dismiss()
        onFileDownloadRequest.value = record
    }

    private val onDeleteRequestObserver = Observer<Void> {
        dismiss()
        onRecordDeleteRequest.value = record
    }

    private val onRenameRequestObserver = Observer<Void> {
        dismiss()
        onRecordRenameRequest.value = record
    }

    private val onShareViaPermanentRequestObserver = Observer<Void> {
        dismiss()
        onRecordShareViaPermanentRequest.value = record
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

    private val onShowMessage = Observer<String> { message ->
        downloadingAlert?.cancel()
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        dismiss()
    }

    private val onRelocateRequestObserver = Observer<RelocationType> {
        dismiss()
        record?.let { record -> onRecordRelocateRequest.value = Pair(record, it) }
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

    fun getOnRecordRenameRequest(): MutableLiveData<Record> = onRecordRenameRequest

    fun getOnRecordShareViaPermanentRequest(): MutableLiveData<Record> =
        onRecordShareViaPermanentRequest

    fun getOnRecordRelocateRequest(): MutableLiveData<Pair<Record, RelocationType>> =
        onRecordRelocateRequest

    override fun connectViewModelEvents() {
        viewModel.getOnRequestWritePermission().observe(this, onRequestWritePermission)
        viewModel.getOnFileDownloadRequest().observe(this, onFileDownloadRequestObserver)
        viewModel.getOnDeleteRequest().observe(this, onDeleteRequestObserver)
        viewModel.getOnRenameRequest().observe(this, onRenameRequestObserver)
        viewModel.getOnShareViaPermanentRequest().observe(this, onShareViaPermanentRequestObserver)
        viewModel.getOnShareToAnotherAppRequest().observe(this, onShareToAnotherAppObserver)
        viewModel.getOnRelocateRequest().observe(this, onRelocateRequestObserver)
        viewModel.getOnFileDownloadedForSharing().observe(this, onFileDownloadedForSharing)
        viewModel.getShowMessage().observe(this, onShowMessage)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnRequestWritePermission().removeObserver(onRequestWritePermission)
        viewModel.getOnFileDownloadRequest().removeObserver(onFileDownloadRequestObserver)
        viewModel.getOnDeleteRequest().removeObserver(onDeleteRequestObserver)
        viewModel.getOnRenameRequest().removeObserver(onRenameRequestObserver)
        viewModel.getOnShareViaPermanentRequest().removeObserver(onShareViaPermanentRequestObserver)
        viewModel.getOnShareToAnotherAppRequest().removeObserver(onShareToAnotherAppObserver)
        viewModel.getOnRelocateRequest().removeObserver(onRelocateRequestObserver)
        viewModel.getOnFileDownloadedForSharing().removeObserver(onFileDownloadedForSharing)
        viewModel.getShowMessage().removeObserver(onShowMessage)
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