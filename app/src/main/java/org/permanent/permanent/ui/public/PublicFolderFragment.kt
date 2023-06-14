package org.permanent.permanent.ui.public

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentPublicFolderBinding
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.Workspace
import org.permanent.permanent.ui.fileView.FileViewOptionsFragment
import org.permanent.permanent.ui.myFiles.*
import org.permanent.permanent.ui.shares.PreviewState
import org.permanent.permanent.viewmodels.PublicFolderViewModel

class PublicFolderFragment : PermanentBaseFragment(), RecordListener {

    private lateinit var viewModel: PublicFolderViewModel
    private lateinit var binding: FragmentPublicFolderBinding
    private lateinit var recordsRecyclerView: RecyclerView
    private lateinit var recordsAdapter: RecordsGridAdapter
    private lateinit var prefsHelper: PreferencesHelper
    private var recordOptionsFragment: RecordOptionsFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[PublicFolderViewModel::class.java]
        binding = FragmentPublicFolderBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        viewModel.setRootFolder(arguments?.getParcelable(PARCELABLE_RECORD_KEY))
        prefsHelper = PreferencesHelper(
            requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        )
        initRecordsRecyclerView(binding.rvRecords)
        activity?.toolbar?.menu?.findItem(R.id.settingsItem)?.isVisible = false
        activity?.toolbar?.menu?.findItem(R.id.moreItem)?.isVisible = true
        return binding.root
    }

    private val onShowMessage = Observer<String> { message ->
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private val onRecordsRetrieved = Observer<MutableList<Record>> {
        recordsAdapter.setRecords(it)
    }

    private val onFileViewRequest = Observer<ArrayList<Record>> {
        val bundle = bundleOf(PARCELABLE_FILES_KEY to it)
        requireParentFragment().findNavController()
            .navigate(R.id.action_publicFolderFragment_to_fileActivity, bundle)
    }

    private val onFolderNameChanged = Observer<String> {
        (activity as AppCompatActivity?)?.supportActionBar?.title = it
    }

    private fun initRecordsRecyclerView(rvRecords: RecyclerView) {
        recordsRecyclerView = rvRecords
        recordsAdapter = RecordsGridAdapter(
            this, false,
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

    fun onMoreItemClick() {
        val fileViewOptionsFragment = FileViewOptionsFragment()
        fileViewOptionsFragment.setBundleArguments(viewModel.getCurrentFolder(), null)
        fileViewOptionsFragment.show(parentFragmentManager, fileViewOptionsFragment.tag)
    }

    fun onNavigateUp(): Boolean {
        return viewModel.onNavigateUp()
    }

    override fun onRecordOptionsClick(record: Record) {
        recordOptionsFragment = RecordOptionsFragment()
        recordOptionsFragment?.setBundleArguments(record, Workspace.PUBLIC_ARCHIVES)
        recordOptionsFragment?.show(parentFragmentManager, recordOptionsFragment?.tag)
    }

    override fun onRecordCheckBoxClick(record: Record) {}

    override fun onRecordDeleteClick(record: Record) {}

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onShowMessage)
        viewModel.getOnRecordsRetrieved().observe(this, onRecordsRetrieved)
        viewModel.getOnFileViewRequest().observe(this, onFileViewRequest)
        viewModel.getOnFolderNameChanged().observe(this, onFolderNameChanged)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getOnRecordsRetrieved().removeObserver(onRecordsRetrieved)
        viewModel.getOnFileViewRequest().removeObserver(onFileViewRequest)
        viewModel.getOnFolderNameChanged().removeObserver(onFolderNameChanged)
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