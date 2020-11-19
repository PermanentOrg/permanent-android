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
import org.permanent.permanent.databinding.FragmentRecordOptionsBinding
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.viewmodels.RecordOptionsViewModel

const val PARCELABLE_RECORD_KEY = "parcelable_record_key"

class FileOptionsFragment : PermanentBottomSheetFragment() {
    private lateinit var binding: FragmentRecordOptionsBinding
    private lateinit var viewModel: RecordOptionsViewModel
    private var record: RecordVO? = null
    private val onFileDownloadRequest = MutableLiveData<RecordVO>()
    private val onRefreshFolder = MutableLiveData<Void>()

    fun setBundleArguments(record: RecordVO) {
        val bundle = Bundle()
        bundle.putParcelable(PARCELABLE_RECORD_KEY, record)
        this.arguments = bundle
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(RecordOptionsViewModel::class.java)
        binding = FragmentRecordOptionsBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        record = arguments?.getParcelable(PARCELABLE_RECORD_KEY)
        viewModel.setRecord(record)
        return binding.root
    }

    private val onRequestWritePermission = Observer<Void> {
        PermissionsHelper().requestWriteStoragePermission(this)
    }

    private val onFileDownloadRequestObserver = Observer<Void> {
        dismiss()
        onFileDownloadRequest.value = record
    }

    private val onRecordDeleteRequestObserver = Observer<Void> {
        record?.let { record -> viewModel.delete(record) }
    }

    private val onRecordDeletedObserver = Observer<Void> {
        dismiss()
        onRefreshFolder.value = onRefreshFolder.value
        if (record?.typeEnum == RecordVO.Type.Folder)
            Toast.makeText(context, R.string.my_files_folder_deleted, Toast.LENGTH_LONG).show()
        else Toast.makeText(context, R.string.my_files_file_deleted, Toast.LENGTH_LONG).show()
    }

    private val onErrorMessageObserver = Observer<String> {
        dismiss()
        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
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

    fun getOnFileDownloadRequest(): MutableLiveData<RecordVO> {
        return onFileDownloadRequest
    }

    fun getOnRefreshFolder(): MutableLiveData<Void> {
        return onRefreshFolder
    }

    override fun connectViewModelEvents() {
        viewModel.getOnRequestWritePermission().observe(this, onRequestWritePermission)
        viewModel.getOnFileDownloadRequest().observe(this, onFileDownloadRequestObserver)
        viewModel.getOnRecordDeleteRequest().observe(this, onRecordDeleteRequestObserver)
        viewModel.getOnRecordDeleted().observe(this, onRecordDeletedObserver)
        viewModel.getOnErrorMessage().observe(this, onErrorMessageObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnRequestWritePermission().removeObserver(onRequestWritePermission)
        viewModel.getOnFileDownloadRequest().removeObserver(onFileDownloadRequestObserver)
        viewModel.getOnRecordDeleteRequest().removeObserver(onRecordDeleteRequestObserver)
        viewModel.getOnRecordDeleted().removeObserver(onRecordDeletedObserver)
        viewModel.getOnErrorMessage().removeObserver(onErrorMessageObserver)
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