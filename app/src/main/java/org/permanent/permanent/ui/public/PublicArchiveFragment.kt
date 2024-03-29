package org.permanent.permanent.ui.public

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentPublicArchiveBinding
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.Workspace
import org.permanent.permanent.ui.myFiles.PARCELABLE_FILES_KEY
import org.permanent.permanent.ui.myFiles.PARCELABLE_RECORD_KEY
import org.permanent.permanent.ui.myFiles.RecordListener
import org.permanent.permanent.ui.myFiles.RecordOptionsFragment
import org.permanent.permanent.ui.myFiles.RecordsGridAdapter
import org.permanent.permanent.ui.public.PublicFragment.Companion.FILE_ARCHIVE_NR
import org.permanent.permanent.ui.public.PublicFragment.Companion.FOLDER_ARCHIVE_NR
import org.permanent.permanent.ui.public.PublicFragment.Companion.FOLDER_LINK_ID
import org.permanent.permanent.ui.shares.PreviewState
import org.permanent.permanent.viewmodels.PublicArchiveViewModel

class PublicArchiveFragment : PermanentBaseFragment(), RecordListener {

    private lateinit var viewModel: PublicArchiveViewModel
    private lateinit var binding: FragmentPublicArchiveBinding
    private lateinit var recordsRecyclerView: RecyclerView
    private lateinit var recordsAdapter: RecordsGridAdapter
    private lateinit var prefsHelper: PreferencesHelper
    private var recordOptionsFragment: RecordOptionsFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[PublicArchiveViewModel::class.java]
        binding = FragmentPublicArchiveBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        prefsHelper = PreferencesHelper(
            requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        )
        initRecordsRecyclerView(binding.rvRecords)
        arguments?.getString(PublicFragment.ARCHIVE_NR)?.let { viewModel.setArchiveNr(it) }

        val fileArchiveNr = arguments?.getString(FILE_ARCHIVE_NR)

        if (fileArchiveNr.isNullOrEmpty()) { // It's a Folder
            val folderArchiveNr = arguments?.getString(FOLDER_ARCHIVE_NR)
            val folderLinkId = arguments?.getString(FOLDER_LINK_ID)?.toInt()

            if (!folderArchiveNr.isNullOrEmpty() && folderLinkId != null) {
                val record = Record(folderArchiveNr, folderLinkId)
                onFolderViewRequest.onChanged(record)
            }
        } else { // It's a File
            viewModel.getRecord(fileArchiveNr)
        }
        // Removing these for the back navigation to work
        arguments?.remove(FILE_ARCHIVE_NR)
        arguments?.remove(FOLDER_ARCHIVE_NR)
        arguments?.remove(FOLDER_LINK_ID)

        return binding.root
    }

    private val onShowMessage = Observer<String> { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private val onRecordsRetrieved = Observer<MutableList<Record>> {
        recordsAdapter.setRecords(it)
    }

    private val onFileViewRequest = Observer<ArrayList<Record>> {
        val bundle = bundleOf(PARCELABLE_FILES_KEY to it)
        requireParentFragment().findNavController()
            .navigate(R.id.action_publicFragment_to_fileActivity, bundle)
    }

    private val onFolderViewRequest = Observer<Record> {
        val bundle = bundleOf(PARCELABLE_RECORD_KEY to it)
        requireParentFragment().findNavController()
            .navigate(R.id.action_publicFragment_to_publicFolderFragment, bundle)
    }

    private fun initRecordsRecyclerView(rvRecords: RecyclerView) {
        recordsRecyclerView = rvRecords
        recordsAdapter = RecordsGridAdapter(
            this,
            false,
            MutableLiveData(false),
            MutableLiveData(false),
            MutableLiveData(PreviewState.ACCESS_GRANTED),
            isForSharePreviewScreen = false,
            isForSharesScreen = false,
            recordListener = this
        )
        recordsRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = recordsAdapter
            setHasFixedSize(true)
        }
    }

    override fun onRecordClick(record: Record) {
        viewModel.onRecordClick(record)
    }

    override fun onRecordOptionsClick(record: Record) {
        recordOptionsFragment = RecordOptionsFragment()
        recordOptionsFragment?.setBundleArguments(record, Workspace.PUBLIC_ARCHIVES)
        recordOptionsFragment?.show(parentFragmentManager, recordOptionsFragment?.tag)
    }

    override fun onRecordCheckBoxClick(record: Record) {
    }

    override fun onRecordDeleteClick(record: Record) {}

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onShowMessage)
        viewModel.getOnRecordsRetrieved().observe(this, onRecordsRetrieved)
        viewModel.getOnFileViewRequest().observe(this, onFileViewRequest)
        viewModel.getOnFolderViewRequest().observe(this, onFolderViewRequest)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getOnRecordsRetrieved().removeObserver(onRecordsRetrieved)
        viewModel.getOnFileViewRequest().removeObserver(onFileViewRequest)
        viewModel.getOnFolderViewRequest().removeObserver(onFolderViewRequest)
    }

    override fun onStart() {
        super.onStart()
        viewModel.getRootRecords()
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