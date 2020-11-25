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
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.myFiles.download.DownloadsAdapter
import org.permanent.permanent.viewmodels.MyFilesViewModel


class MyFilesFragment : PermanentBaseFragment() {
    private lateinit var binding: FragmentMyFilesBinding
    private lateinit var viewModel: MyFilesViewModel
    private lateinit var downloadsRecyclerView: RecyclerView
    private lateinit var downloadsAdapter: DownloadsAdapter
    private lateinit var filesRecyclerView: RecyclerView
    private lateinit var recordsAdapter: RecordsAdapter
    private var addOptionsFragment: AddOptionsFragment? = null
    private var recordOptionsFragment: RecordOptionsFragment? = null
    private var sortOptionsFragment: SortOptionsFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

    private val onShowMessage = Observer<String> {
        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
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

    private val onRecordsRetrieved = Observer<List<Record>> {
        recordsAdapter.set(it)
    }

    private val onRecordsFilterQuery = Observer<Editable> {
        recordsAdapter.filter.filter(it)
    }

    private val onNewTemporaryFile = Observer<Record> {
        recordsAdapter.add(it)
    }

    private val onShowAddOptionsFragment = Observer<FolderIdentifier> {
        addOptionsFragment = AddOptionsFragment()
        addOptionsFragment?.setBundleArguments(it)
        addOptionsFragment?.show(parentFragmentManager, addOptionsFragment?.tag)
        addOptionsFragment?.getOnRefreshFolder()?.observe(this, onRefreshFolder)
    }

    private val onShowRecordOptionsFragment = Observer<Record> {
        recordOptionsFragment = RecordOptionsFragment()
        recordOptionsFragment?.setBundleArguments(it)
        recordOptionsFragment?.show(parentFragmentManager, recordOptionsFragment?.tag)
        recordOptionsFragment?.getOnFileDownloadRequest()?.observe(this, onFileDownloadRequest)
        recordOptionsFragment?.getOnRecordRelocateRequest()?.observe(this, onRecordRelocateRequest)
        recordOptionsFragment?.getOnRefreshFolder()?.observe(this, onRefreshFolder)
    }

    private val onShowSortOptionsFragment = Observer<SortType> {
        sortOptionsFragment = SortOptionsFragment()
        sortOptionsFragment?.setBundleArguments(it)
        sortOptionsFragment?.show(parentFragmentManager, recordOptionsFragment?.tag)
        sortOptionsFragment?.getOnSortRequest()?.observe(this, onSortRequest)
    }

    private val onFileDownloadRequest = Observer<Record> {
        if (it.type != RecordType.FOLDER) {
            viewModel.download(it)
        }
    }

    private val onRecordRelocateRequest = Observer<Pair<Record, RelocationType>> {
        viewModel.setRelocationMode(it)
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
        recordsAdapter = RecordsAdapter(viewModel, viewModel, this, viewModel.getIsRelocationMode())
        filesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = recordsAdapter
            addItemDecoration(
                DividerItemDecoration(
                    this.context,
                    DividerItemDecoration.VERTICAL)
            )
        }
    }

    override fun connectViewModelEvents() {
        viewModel.getOnShowMessage().observe(this, onShowMessage)
        viewModel.getOnDownloadsRetrieved().observe(this, onDownloadsRetrieved)
        viewModel.getOnDownloadFinished().observe(this, onDownloadFinished)
        viewModel.getOnFilesRetrieved().observe(this, onRecordsRetrieved)
        viewModel.getOnFilesFilterQuery().observe(this, onRecordsFilterQuery)
        viewModel.getOnNewTemporaryFile().observe(this, onNewTemporaryFile)
        viewModel.getOnShowAddOptionsFragment().observe(this, onShowAddOptionsFragment)
        viewModel.getOnShowFileOptionsFragment().observe(this, onShowRecordOptionsFragment)
        viewModel.getOnShowSortOptionsFragment().observe(this, onShowSortOptionsFragment)
        addOptionsFragment?.getOnFilesSelected()?.observe(this, onFilesSelectedToUpload)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnShowMessage().removeObserver(onShowMessage)
        viewModel.getOnDownloadsRetrieved().removeObserver(onDownloadsRetrieved)
        viewModel.getOnDownloadFinished().removeObserver(onDownloadFinished)
        viewModel.getOnFilesRetrieved().removeObserver(onRecordsRetrieved)
        viewModel.getOnFilesFilterQuery().removeObserver(onRecordsFilterQuery)
        viewModel.getOnNewTemporaryFile().removeObserver(onNewTemporaryFile)
        viewModel.getOnShowAddOptionsFragment().removeObserver(onShowAddOptionsFragment)
        viewModel.getOnShowFileOptionsFragment().removeObserver(onShowRecordOptionsFragment)
        viewModel.getOnShowSortOptionsFragment().removeObserver(onShowSortOptionsFragment)
        addOptionsFragment?.getOnFilesSelected()?.removeObserver(onFilesSelectedToUpload)
        addOptionsFragment?.getOnRefreshFolder()?.removeObserver(onRefreshFolder)
        recordOptionsFragment?.getOnFileDownloadRequest()?.removeObserver(onFileDownloadRequest)
        recordOptionsFragment?.getOnRecordRelocateRequest()?.removeObserver(onRecordRelocateRequest)
        recordOptionsFragment?.getOnRefreshFolder()?.removeObserver(onRefreshFolder)
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