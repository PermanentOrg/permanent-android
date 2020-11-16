package org.permanent.permanent.ui.myFiles

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.permanent.permanent.PermissionsHelper
import org.permanent.permanent.R
import org.permanent.permanent.REQUEST_CODE_WRITE_STORAGE_PERMISSION
import org.permanent.permanent.databinding.FragmentFileOptionsBinding
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.viewmodels.FileOptionsViewModel

const val FILE_NAME = "file_name"

class FileOptionsFragment : PermanentBottomSheetFragment() {
    private lateinit var binding: FragmentFileOptionsBinding
    private lateinit var viewModel: FileOptionsViewModel
    private val onFileDownloadRequest = MutableLiveData<Void>()

    fun setBundleArguments(fileDisplayName: String?) {
        val bundle = Bundle()
        bundle.putString(FILE_NAME, fileDisplayName)
        this.arguments = bundle
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(FileOptionsViewModel::class.java)
        binding = FragmentFileOptionsBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.tvFileName.text = arguments?.getString(FILE_NAME)

        return binding.root
    }

    private val onRequestWritePermission = Observer<Void> {
        PermissionsHelper().requestWriteStoragePermission(this)
    }

    private val onFileDownloadRequestObserver = Observer<Void> {
        dismiss()
        onFileDownloadRequest.value = onFileDownloadRequest.value
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
                        context, R.string.download_no_permissions_error, Toast.LENGTH_LONG).show()
                }
        }
    }

    fun getOnFileDownloadRequest(): MutableLiveData<Void> {
        return onFileDownloadRequest
    }

    override fun connectViewModelEvents() {
        viewModel.getOnRequestWritePermission().observe(this, onRequestWritePermission)
        viewModel.getOnFileDownloadRequest().observe(this, onFileDownloadRequestObserver)
    }

    override fun disconnectViewModelEvents() {
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