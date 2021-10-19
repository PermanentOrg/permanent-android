package org.permanent.permanent.ui.shares

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentSharedXMeBinding
import org.permanent.permanent.models.Download
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.myFiles.*
import org.permanent.permanent.ui.myFiles.download.DownloadsAdapter
import org.permanent.permanent.viewmodels.SharedXMeViewModel
import org.permanent.permanent.viewmodels.SingleLiveEvent

class SharedXMeFragment : PermanentBaseFragment(), RecordListener {

    private lateinit var viewModel: SharedXMeViewModel
    private lateinit var binding: FragmentSharedXMeBinding
    private lateinit var downloadsRecyclerView: RecyclerView
    private lateinit var downloadsAdapter: DownloadsAdapter
    private lateinit var recordsRecyclerView: RecyclerView
    private lateinit var recordsAdapter: RecordsAdapter
    private lateinit var recordsListAdapter: RecordsListAdapter
    private lateinit var recordsGridAdapter: RecordsGridAdapter
    private lateinit var record: Record
    private lateinit var prefsHelper: PreferencesHelper
    private val getRootRecords = SingleLiveEvent<Void>()
    private var recordOptionsFragment: RecordOptionsFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(SharedXMeViewModel::class.java)
        binding = FragmentSharedXMeBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        viewModel.setLifecycleOwner(this)
        prefsHelper = PreferencesHelper(
            requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        )
        initDownloadsRecyclerView(binding.rvDownloads)
        initRecordsRecyclerView(binding.rvShares)
        arguments?.takeIf { it.containsKey(SHARED_X_ME_NO_ITEMS_MESSAGE_KEY) }?.apply {
            getString(SHARED_X_ME_NO_ITEMS_MESSAGE_KEY).also { binding.tvNoShares.text = it }
        }
        arguments?.takeIf { it.containsKey(SHARED_WITH_ME_ITEM_LIST_KEY) }?.apply {
            getParcelableArrayList<Record>(SHARED_WITH_ME_ITEM_LIST_KEY).also {
                if (!it.isNullOrEmpty()) recordsAdapter.setRecords(it)
                viewModel.getExistsShares().value = !it.isNullOrEmpty()
            }
        }
        return binding.root
    }

    private val onShowMessage = Observer<String> { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private val onDownloadsRetrieved = Observer<MutableList<Download>> {
        downloadsAdapter.set(it)
    }

    private val onDownloadFinished = Observer<Download> { download ->
        downloadsAdapter.remove(download)
    }

    private val onRecordsRetrieved = Observer<MutableList<Record>> {
        recordsAdapter.setRecords(it)
    }

    private val onRootSharesNeeded = Observer<Void> { getRootRecords.call() }

    private val onChangeViewMode = Observer<Boolean> { isListViewMode ->
        prefsHelper.saveIsListViewMode(isListViewMode)
        val records = recordsAdapter.getRecords()
        recordsRecyclerView.apply {
            if (isListViewMode) {
                layoutManager = LinearLayoutManager(context)
                recordsAdapter = recordsListAdapter
            } else {
                layoutManager = GridLayoutManager(context, 2)
                recordsAdapter = recordsGridAdapter
            }
            adapter = recordsAdapter
            recordsAdapter.setRecords(records)
        }
    }

    private val onFileViewRequest = Observer<Record> {
        val files = ArrayList<Record>()
        files.add(it)
        val bundle = bundleOf(PARCELABLE_FILES_KEY to files)
        requireParentFragment().findNavController()
            .navigate(R.id.action_sharesFragment_to_fileActivity, bundle)
    }

    private val onFileDownloadRequest = Observer<Record> {
        viewModel.download(it)
    }

    private fun initRecordsRecyclerView(rvRecords: RecyclerView) {
        recordsRecyclerView = rvRecords
        recordsListAdapter = RecordsListAdapter(
            this, MutableLiveData(false), true, this
        )
        recordsGridAdapter = RecordsGridAdapter(
            this,
            MutableLiveData(false),
            MutableLiveData(PreviewState.ACCESS_GRANTED),
            isForSharePreviewScreen = false,
            isForSharesScreen = true,
            recordListener = this
        )
        val isListViewMode = prefsHelper.isListViewMode()
        viewModel.setIsListViewMode(isListViewMode)
        recordsRecyclerView.apply {
            if (isListViewMode) {
                recordsAdapter = recordsListAdapter
                layoutManager = LinearLayoutManager(context)
            } else {
                recordsAdapter = recordsGridAdapter
                layoutManager = GridLayoutManager(context, 2)
            }
            adapter = recordsAdapter
            addItemDecoration(
                DividerItemDecoration(
                    this.context,
                    DividerItemDecoration.VERTICAL
                )
            )
            setHasFixedSize(true)
        }
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

    fun setShares(records: MutableList<Record>) {
        recordsAdapter.setRecords(records)
        viewModel.getIsRoot().value = true
        viewModel.getExistsShares().value = true
    }

    fun navigateToRecord(recordIdToNavigateTo: Int) {
        recordsAdapter.getItemById(recordIdToNavigateTo)?.let { record ->
            viewModel.onRecordClick(record)
        }
    }

    fun getRootShares(): LiveData<Void> {
        return getRootRecords
    }

    override fun onRecordClick(record: Record) {
        viewModel.onRecordClick(record)
    }

    override fun onRecordOptionsClick(record: Record) {
        this.record = record
        recordOptionsFragment = RecordOptionsFragment()
        recordOptionsFragment?.setBundleArguments(record, false)
        recordOptionsFragment?.show(parentFragmentManager, recordOptionsFragment?.tag)
        recordOptionsFragment?.getOnFileDownloadRequest()?.observe(this, onFileDownloadRequest)
    }

    override fun onRecordDeleteClick(record: Record) {}

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onShowMessage)
        viewModel.getOnDownloadsRetrieved().observe(this, onDownloadsRetrieved)
        viewModel.getOnDownloadFinished().observe(this, onDownloadFinished)
        viewModel.getOnRecordsRetrieved().observe(this, onRecordsRetrieved)
        viewModel.getOnRootSharesNeeded().observe(this, onRootSharesNeeded)
        viewModel.getOnChangeViewMode().observe(this, onChangeViewMode)
        viewModel.getOnFileViewRequest().observe(this, onFileViewRequest)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getOnDownloadsRetrieved().removeObserver(onDownloadsRetrieved)
        viewModel.getOnDownloadFinished().removeObserver(onDownloadFinished)
        viewModel.getOnRecordsRetrieved().removeObserver(onRecordsRetrieved)
        viewModel.getOnRootSharesNeeded().removeObserver(onRootSharesNeeded)
        viewModel.getOnChangeViewMode().removeObserver(onChangeViewMode)
        viewModel.getOnFileViewRequest().removeObserver(onFileViewRequest)
        recordOptionsFragment?.getOnFileDownloadRequest()?.removeObserver(onFileDownloadRequest)
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