package org.permanent.permanent.ui.myFiles

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.databinding.FragmentMyFilesBinding
import org.permanent.permanent.models.Download
import org.permanent.permanent.models.FolderIdentifier
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.myFiles.download.DownloadsAdapter
import org.permanent.permanent.viewmodels.MyFilesViewModel


class MyFilesFragment : PermanentBaseFragment() {
    private lateinit var binding: FragmentMyFilesBinding
    private lateinit var viewModel: MyFilesViewModel
    private lateinit var downloadsRecyclerView: RecyclerView
    private lateinit var downloadsAdapter: DownloadsAdapter
    private lateinit var filesRecyclerView: RecyclerView
    private lateinit var filesAdapter: FilesAdapter
    private var addOptionsFragment: AddOptionsFragment? = null
    private var fileOptionsFragment: FileOptionsFragment? = null
    private var sortOptionsFragment: SortOptionsFragment? = null

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
        initDownloadsRecyclerView(binding.rvDownloads)
        initFilesRecyclerView(binding.rvFiles)
        return binding.root
    }

    private val onFilesSelectedToUpload = Observer<MutableList<Uri>> { fileUriList ->
        if (fileUriList.isNotEmpty()) {
            viewModel.enqueueFilesForUpload(fileUriList)
            fileUriList.clear()
        }
    }

    private val onDownloadFinished = Observer<Download> { download ->
        Toast.makeText(context, "Downloaded ${download.getDisplayName()}",
            Toast.LENGTH_LONG).show()
        downloadsAdapter.remove(download)
    }

    private val onDownloadsRetrieved = Observer<MutableList<Download>> {
        downloadsAdapter.set(it)
    }

    private val onFilesRetrieved = Observer<List<RecordVO>> {
        filesAdapter.set(it)
    }

    private val onFilesFilterQuery = Observer<Editable> {
        filesAdapter.filter.filter(it)
    }

    private val onNewTemporaryFile = Observer<RecordVO> {
        filesAdapter.add(it)
    }

    private val onShowAddOptionsFragment = Observer<FolderIdentifier> {
        addOptionsFragment = AddOptionsFragment()
        addOptionsFragment?.setBundleArguments(it)
        addOptionsFragment?.show(parentFragmentManager, addOptionsFragment?.tag)
        addOptionsFragment?.getOnRefreshFolder()?.observe(this, onRefreshFolder)
    }

    private val onShowFileOptionsFragment = Observer<RecordVO> {
        fileOptionsFragment = FileOptionsFragment()
        fileOptionsFragment?.setBundleArguments(it)
        fileOptionsFragment?.show(parentFragmentManager, fileOptionsFragment?.tag)
        fileOptionsFragment?.getOnFileDownloadRequest()?.observe(this, onFileDownloadRequest)
        fileOptionsFragment?.getOnRefreshFolder()?.observe(this, onRefreshFolder)
    }

    private val onShowSortOptionsFragment = Observer<SortType> {
        sortOptionsFragment = SortOptionsFragment()
        sortOptionsFragment?.setBundleArguments(it)
        sortOptionsFragment?.show(parentFragmentManager, fileOptionsFragment?.tag)
        sortOptionsFragment?.getOnSortRequest()?.observe(this, onSortRequest)
    }

    private val onFileDownloadRequest = Observer<RecordVO> {
        if (it.typeEnum != RecordVO.Type.Folder) {
            viewModel.download(it)
        }
    }

    private val onSortRequest = Observer<SortType> {
        viewModel.setSortType(it)
    }

    private val onRefreshFolder = Observer<Void> {
        viewModel.refreshCurrentFolder()
    }

    private fun initDownloadsRecyclerView(rvDownloads: RecyclerView) {
        downloadsRecyclerView = rvDownloads
        downloadsAdapter = DownloadsAdapter(this, viewModel)
        viewModel.setExistsDownloads(downloadsAdapter.getExistsDownloads())
        downloadsRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = downloadsAdapter
        }
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

    override fun connectViewModelEvents() {
        viewModel.getOnDownloadsRetrieved().observe(this, onDownloadsRetrieved)
        viewModel.getOnDownloadFinished().observe(this, onDownloadFinished)
        viewModel.getOnFilesRetrieved().observe(this, onFilesRetrieved)
        viewModel.getOnFilesFilterQuery().observe(this, onFilesFilterQuery)
        viewModel.getOnNewTemporaryFile().observe(this, onNewTemporaryFile)
        viewModel.getOnShowAddOptionsFragment().observe(this, onShowAddOptionsFragment)
        viewModel.getOnShowFileOptionsFragment().observe(this, onShowFileOptionsFragment)
        viewModel.getOnShowSortOptionsFragment().observe(this, onShowSortOptionsFragment)
        addOptionsFragment?.getOnFilesSelected()?.observe(this, onFilesSelectedToUpload)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnDownloadsRetrieved().removeObserver(onDownloadsRetrieved)
        viewModel.getOnDownloadFinished().removeObserver(onDownloadFinished)
        viewModel.getOnFilesRetrieved().removeObserver(onFilesRetrieved)
        viewModel.getOnFilesFilterQuery().removeObserver(onFilesFilterQuery)
        viewModel.getOnNewTemporaryFile().removeObserver(onNewTemporaryFile)
        viewModel.getOnShowAddOptionsFragment().removeObserver(onShowAddOptionsFragment)
        viewModel.getOnShowFileOptionsFragment().removeObserver(onShowFileOptionsFragment)
        viewModel.getOnShowSortOptionsFragment().removeObserver(onShowSortOptionsFragment)
        addOptionsFragment?.getOnFilesSelected()?.removeObserver(onFilesSelectedToUpload)
        addOptionsFragment?.getOnRefreshFolder()?.removeObserver(onRefreshFolder)
        fileOptionsFragment?.getOnFileDownloadRequest()?.removeObserver(onFileDownloadRequest)
        fileOptionsFragment?.getOnRefreshFolder()?.removeObserver(onRefreshFolder)
        sortOptionsFragment?.getOnSortRequest()?.removeObserver(onSortRequest)
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