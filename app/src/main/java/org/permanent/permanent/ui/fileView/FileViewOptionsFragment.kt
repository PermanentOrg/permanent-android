package org.permanent.permanent.ui.fileView

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentFileViewOptionsBinding
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.models.FileData
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.ui.myFiles.PARCELABLE_RECORD_KEY
import org.permanent.permanent.viewmodels.FileViewOptionsViewModel

class FileViewOptionsFragment : PermanentBottomSheetFragment() {
    private lateinit var binding: FragmentFileViewOptionsBinding
    private lateinit var viewModel: FileViewOptionsViewModel
    private var record: Record? = null
    private var fileData: FileData? = null
    private var downloadingAlert: AlertDialog? = null

    fun setBundleArguments(record: Record?, fileData: FileData?) {
        val bundle = Bundle()
        bundle.putParcelable(PARCELABLE_RECORD_KEY, record)
        bundle.putParcelable(PARCELABLE_FILE_DATA_KEY, fileData)
        this.arguments = bundle
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(FileViewOptionsViewModel::class.java)
        binding = FragmentFileViewOptionsBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        record = arguments?.getParcelable(PARCELABLE_RECORD_KEY)
        fileData = arguments?.getParcelable(PARCELABLE_FILE_DATA_KEY)
        viewModel.setArguments(record, fileData)
        return binding.root
    }

    private val onShareViaPermanentObserver = Observer<Void> {
        val bundle = bundleOf(PARCELABLE_RECORD_KEY to record)
        requireParentFragment().requireParentFragment().findNavController()
            .navigate(R.id.action_filesContainerFragment_to_shareLinkFragment, bundle)
        dismiss()
    }

    private val onShareToAnotherAppObserver = Observer<Void> {
        viewModel.getUriForSharing()?.let {
            shareFile(it)
            dismiss()
        } ?: run {
            downloadingAlert = context?.let {
                AlertDialog.Builder(it)
                    .setTitle(getString(R.string.downloading_file_in_progress))
                    .setNegativeButton(
                        getString(R.string.button_cancel)
                    ) { _, _ -> viewModel.cancelDownload() }
                    .create()
            }
            downloadingAlert?.show()
            viewModel.downloadFile(this)
        }
    }

    private val onFileDownloaded = Observer<Void> {
        downloadingAlert?.cancel()
        viewModel.getUriForSharing()?.let { shareFile(it) }
        dismiss()
    }

    private val onShowMessage = Observer<String> { message ->
        downloadingAlert?.cancel()
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        dismiss()
    }

    private fun shareFile(sharingUri: Uri) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_STREAM, sharingUri)
        intent.type = fileData?.contentType
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(intent, ""))
    }

    override fun connectViewModelEvents() {
        viewModel.getOnShareViaPermanentRequest().observe(this, onShareViaPermanentObserver)
        viewModel.getOnShareToAnotherAppRequest().observe(this, onShareToAnotherAppObserver)
        viewModel.getOnFileDownloaded().observe(this, onFileDownloaded)
        viewModel.getShowMessage().observe(this, onShowMessage)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnShareViaPermanentRequest().removeObserver(onShareViaPermanentObserver)
        viewModel.getOnShareToAnotherAppRequest().removeObserver(onShareToAnotherAppObserver)
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getOnFileDownloaded().removeObserver(onFileDownloaded)
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