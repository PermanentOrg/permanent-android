package org.permanent.permanent.ui.myFiles.saveToPermanent

import android.app.Dialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.permanent.permanent.databinding.FragmentSaveToPermanentBinding
import org.permanent.permanent.models.File
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.ui.activities.MainActivity
import org.permanent.permanent.viewmodels.SaveToPermanentViewModel
import org.permanent.permanent.viewmodels.SingleLiveEvent


class SaveToPermanentFragment : PermanentBottomSheetFragment() {
    private lateinit var binding: FragmentSaveToPermanentBinding
    private lateinit var viewModel: SaveToPermanentViewModel
    private lateinit var filesRecyclerView: RecyclerView
    private lateinit var filesAdapter: FilesAdapter
    private val onFilesUploadRequest = SingleLiveEvent<Void>()

    fun setBundleArguments(
        uris: ArrayList<Uri>
    ) {
        val bundle = bundleOf(MainActivity.SAVE_TO_PERMANENT_FILE_URIS_KEY to uris)
        this.arguments = bundle
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[SaveToPermanentViewModel::class.java]
        binding = FragmentSaveToPermanentBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        arguments?.getParcelableArrayList<Uri>(MainActivity.SAVE_TO_PERMANENT_FILE_URIS_KEY)?.let {
            initFilesRecyclerView(binding.rvFiles, viewModel.getFiles(it))
        }

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener { dialog: DialogInterface ->
            val dialogc = dialog as BottomSheetDialog
            val bottomSheet =
                dialogc.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from(bottomSheet as FrameLayout)
                .setState(BottomSheetBehavior.STATE_EXPANDED)
        }
        return bottomSheetDialog
    }

    private fun initFilesRecyclerView(rvFiles: RecyclerView, files: ArrayList<File>) {
        filesRecyclerView = rvFiles
        filesAdapter = FilesAdapter(files)
        filesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = filesAdapter
        }
    }

    private val onUploadRequestObserver = Observer<Void> {
        dismiss()
        onFilesUploadRequest.call()
    }

    private val onCancelRequestObserver = Observer<Void> {
        dismiss()
    }

    fun getOnFilesUploadRequest(): MutableLiveData<Void> = onFilesUploadRequest

    override fun connectViewModelEvents() {
        viewModel.getOnUploadRequest().observe(this, onUploadRequestObserver)
        viewModel.getOnCancelRequest().observe(this, onCancelRequestObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnUploadRequest().removeObserver(onUploadRequestObserver)
        viewModel.getOnCancelRequest().removeObserver(onCancelRequestObserver)
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