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
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.viewmodels.RecordOptionsViewModel

const val PARCELABLE_RECORD_KEY = "parcelable_record_key"

class RecordOptionsFragment : PermanentBottomSheetFragment() {
    private lateinit var binding: FragmentRecordOptionsBinding
    private lateinit var viewModel: RecordOptionsViewModel
    private var record: Record? = null
    private val onFileDownloadRequest = MutableLiveData<Record>()
    private val onRecordDeleteRequest = MutableLiveData<Record>()
    private val onRecordRelocateRequest = MutableLiveData<Pair<Record, RelocationType>>()
    private val onRefreshFolder = MutableLiveData<Void>()

    fun setBundleArguments(record: Record) {
        val bundle = Bundle()
        bundle.putParcelable(PARCELABLE_RECORD_KEY, record)
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
        dismiss()
        onRecordDeleteRequest.value = record
    }

    private val onRelocateRequestObserver = Observer<RelocationType> {
        dismiss()
        record?.let { record ->  onRecordRelocateRequest.value = Pair(record, it) }
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

    fun getOnFileDownloadRequest(): MutableLiveData<Record> {
        return onFileDownloadRequest
    }

    fun getOnRecordDeleteRequest(): MutableLiveData<Record> {
        return onRecordDeleteRequest
    }

    fun getOnRecordRelocateRequest(): MutableLiveData<Pair<Record, RelocationType>> {
        return onRecordRelocateRequest
    }

    fun getOnRefreshFolder(): MutableLiveData<Void> {
        return onRefreshFolder
    }

    override fun connectViewModelEvents() {
        viewModel.getOnRequestWritePermission().observe(this, onRequestWritePermission)
        viewModel.getOnFileDownloadRequest().observe(this, onFileDownloadRequestObserver)
        viewModel.getOnRecordDeleteRequest().observe(this, onRecordDeleteRequestObserver)
        viewModel.getOnRelocateRequest().observe(this, onRelocateRequestObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnRequestWritePermission().removeObserver(onRequestWritePermission)
        viewModel.getOnFileDownloadRequest().removeObserver(onFileDownloadRequestObserver)
        viewModel.getOnRecordDeleteRequest().removeObserver(onRecordDeleteRequestObserver)
        viewModel.getOnRelocateRequest().removeObserver(onRelocateRequestObserver)
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