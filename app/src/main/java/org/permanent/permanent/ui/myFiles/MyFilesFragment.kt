package org.permanent.permanent.ui.myFiles

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.FragmentMyFilesBinding
import org.permanent.permanent.models.FolderIdentifier
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.MyFilesViewModel


class MyFilesFragment : PermanentBaseFragment() {
    private lateinit var binding: FragmentMyFilesBinding
    private lateinit var viewModel: MyFilesViewModel
    private lateinit var filesRecyclerView: RecyclerView
    private lateinit var filesAdapter: FilesAdapter
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
        viewModel.initUploadsRecyclerView(binding.rvUploads, this)
        viewModel.initSwipeRefreshLayout(binding.swipeRefreshLayout)
        initFilesRecyclerView(binding.rvFiles)
        return binding.root
    }

    private val onFilesSelectedToUpload = Observer<MutableList<Uri>> { fileUriList ->
        if (fileUriList.isNotEmpty()) {
            viewModel.enqueueFilesForUpload(fileUriList)
            fileUriList.clear()
        }
    }

    private val onFilesRetrieved = Observer<List<RecordVO>> {
        filesAdapter.set(it)
    }

    private val onFilesFilterQuery = Observer<Editable> {
        filesAdapter.filter.filter(it)
    }

    private val onNewFile = Observer<RecordVO> {
        filesAdapter.add(it)
    }

    private val onShowAddOptionsFragment = Observer<FolderIdentifier> {
        addOptionsFragment = AddOptionsFragment()
        addOptionsFragment?.setBundle(it)
        addOptionsFragment?.show(parentFragmentManager, addOptionsFragment?.tag)
    }

    private fun initFilesRecyclerView(rvFiles: RecyclerView) {
        filesRecyclerView = rvFiles
        filesAdapter = FilesAdapter(viewModel, viewModel)
        filesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = filesAdapter
            addItemDecoration(
                DividerItemDecoration(
                    this.context,
                    DividerItemDecoration.VERTICAL)
            )
        }
    }

    fun refreshCurrentFolder() {
        viewModel.refreshCurrentFolder()
    }

    override fun connectViewModelEvents() {
        viewModel.getOnFilesRetrieved().observe(this, onFilesRetrieved)
        viewModel.getOnFilesFilterQuery().observe(this, onFilesFilterQuery)
        viewModel.getOnNewFile().observe(this, onNewFile)
        viewModel.getOnShowAddOptionsFragment().observe(this, onShowAddOptionsFragment)
        addOptionsFragment?.getOnFilesSelected()?.observe(this, onFilesSelectedToUpload)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnFilesRetrieved().removeObserver(onFilesRetrieved)
        viewModel.getOnFilesFilterQuery().removeObserver(onFilesFilterQuery)
        viewModel.getOnNewFile().removeObserver(onNewFile)
        viewModel.getOnShowAddOptionsFragment().removeObserver(onShowAddOptionsFragment)
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