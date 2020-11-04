package org.permanent.permanent.ui.myFiles

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.permanent.permanent.databinding.FragmentMyFilesBinding
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.myFiles.upload.UPLOAD_PROGRESS
import org.permanent.permanent.viewmodels.MyFilesViewModel


class MyFilesFragment : PermanentBaseFragment(), View.OnClickListener {

    private lateinit var binding: FragmentMyFilesBinding
    private lateinit var viewModel: MyFilesViewModel
    private var addOptionsFragment: AddOptionsFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyFilesBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(MyFilesViewModel::class.java)
        binding.viewModel = viewModel
        viewModel.set(parentFragmentManager)
        viewModel.initUploadsRecyclerView(binding.rvUploads)
        viewModel.initFilesRecyclerView(binding.rvFiles)
        viewModel.initSwipeRefreshLayout(binding.swipeRefreshLayout)
        binding.fabAdd.setOnClickListener(this)

        return binding.root
    }

    // On fabAdd click
    override fun onClick(view: View) {
        addOptionsFragment = AddOptionsFragment()
        addOptionsFragment?.show(parentFragmentManager, addOptionsFragment?.tag)
    }

    private val onFilesSelectedToUpload = Observer<List<Uri>> { files ->
        val uploadWorkInfos = viewModel.upload(files)

        for (workInfoLiveData in uploadWorkInfos) {
            workInfoLiveData.observe(this, {
                val progress = it.progress.getInt(UPLOAD_PROGRESS, 0)
                viewModel.onUploadStateChanged(it.id, it.state, progress)
            })
        }
    }

    fun refreshCurrentFolder() {
        viewModel.refreshCurrentFolder()
    }

    override fun connectViewModelEvents() {
        addOptionsFragment?.getOnFilesSelected()?.observe(this, onFilesSelectedToUpload)
    }

    override fun disconnectViewModelEvents() {
        addOptionsFragment?.getOnFilesSelected()?.removeObserver(onFilesSelectedToUpload)
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