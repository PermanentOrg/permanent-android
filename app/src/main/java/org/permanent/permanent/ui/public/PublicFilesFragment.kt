package org.permanent.permanent.ui.public

import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.R
import org.permanent.permanent.databinding.DialogCancelUploadsBinding
import org.permanent.permanent.databinding.DialogDeleteBinding
import org.permanent.permanent.databinding.DialogRenameRecordBinding
import org.permanent.permanent.databinding.FragmentPublicFilesBinding
import org.permanent.permanent.models.Download
import org.permanent.permanent.models.NavigationFolderIdentifier
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.SelectionOptionsFragment
import org.permanent.permanent.ui.Workspace
import org.permanent.permanent.ui.hideKeyboardFrom
import org.permanent.permanent.ui.myFiles.AddOptionsFragment
import org.permanent.permanent.ui.myFiles.MyFilesFragment.Companion.DELAY_TO_RESIZE_MILLIS
import org.permanent.permanent.ui.myFiles.MyFilesFragment.Companion.ISLAND_WIDTH_LARGE
import org.permanent.permanent.ui.myFiles.MyFilesFragment.Companion.ISLAND_WIDTH_SMALL
import org.permanent.permanent.ui.myFiles.MyFilesFragment.Companion.RESIZE_DURATION_MILLIS
import org.permanent.permanent.ui.myFiles.PARCELABLE_FILES_KEY
import org.permanent.permanent.ui.myFiles.PARCELABLE_RECORD_KEY
import org.permanent.permanent.ui.myFiles.RecordOptionsFragment
import org.permanent.permanent.ui.myFiles.RecordsAdapter
import org.permanent.permanent.ui.myFiles.RecordsGridAdapter
import org.permanent.permanent.ui.myFiles.RecordsListAdapter
import org.permanent.permanent.ui.myFiles.RelocationType
import org.permanent.permanent.ui.myFiles.SortOptionsFragment
import org.permanent.permanent.ui.myFiles.SortType
import org.permanent.permanent.ui.myFiles.download.DownloadsAdapter
import org.permanent.permanent.ui.shares.PreviewState
import org.permanent.permanent.ui.shares.SHOW_SCREEN_SIMPLIFIED_KEY
import org.permanent.permanent.viewmodels.PublicFilesViewModel
import org.permanent.permanent.viewmodels.RenameRecordViewModel
import org.permanent.permanent.viewmodels.SingleLiveEvent

class PublicFilesFragment : PermanentBaseFragment() {
    private lateinit var binding: FragmentPublicFilesBinding
    private lateinit var viewModel: PublicFilesViewModel
    private lateinit var downloadsRecyclerView: RecyclerView
    private lateinit var downloadsAdapter: DownloadsAdapter
    private lateinit var recordsRecyclerView: RecyclerView
    private lateinit var recordsAdapter: RecordsAdapter
    private lateinit var recordsListAdapter: RecordsListAdapter
    private lateinit var recordsGridAdapter: RecordsGridAdapter
    private lateinit var renameDialogViewModel: RenameRecordViewModel
    private lateinit var renameDialogBinding: DialogRenameRecordBinding
    private var alertDialog: androidx.appcompat.app.AlertDialog? = null
    private lateinit var prefsHelper: PreferencesHelper
    private var shouldRefreshCurrentFolder: Boolean = false
    private var addOptionsFragment: AddOptionsFragment? = null
    private var recordOptionsFragment: RecordOptionsFragment? = null
    private var sortOptionsFragment: SortOptionsFragment? = null
    private var selectionOptionsFragment: SelectionOptionsFragment? = null
    private val onRecordSelectedEvent = SingleLiveEvent<Pair<Workspace, Record>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPublicFilesBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[PublicFilesViewModel::class.java]
        renameDialogViewModel = ViewModelProvider(this)[RenameRecordViewModel::class.java]
        prefsHelper = PreferencesHelper(
            requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        )
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.set(parentFragmentManager)
        viewModel.initUploadsRecyclerView(binding.rvUploads, this)
        viewModel.initSwipeRefreshLayout(binding.swipeRefreshLayout)
        viewModel.loadRootFiles()
        initDownloadsRecyclerView(binding.rvDownloads)
        initFilesRecyclerView(binding.rvFiles)

        if (viewModel.isRelocationMode.value == true) resizeIslandWidthAnimated(
            binding.flFloatingActionIsland.width, ISLAND_WIDTH_LARGE
        )
        arguments?.takeIf { it.containsKey(SHOW_SCREEN_SIMPLIFIED_KEY) }?.apply {
            val showScreenSimplified = getBoolean(SHOW_SCREEN_SIMPLIFIED_KEY)
            if (showScreenSimplified) viewModel.setShowScreenSimplified()
        }

        return binding.root
    }

    private val onShowMessage = Observer<String> {
        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
    }

    private val onShowQuotaExceeded = Observer<Void> {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(activity)
        alertDialog.setTitle(R.string.my_files_quota_exceeded_title)
        alertDialog.setMessage(R.string.my_files_quota_exceeded_message)
        alertDialog.setPositiveButton(
            R.string.yes_button
        ) { _, _ ->
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(BuildConfig.ADD_STORAGE_URL)
            startActivity(intent)
        }
        alertDialog.setNegativeButton(
            R.string.no_button
        ) { _, _ -> }
        val alert: AlertDialog = alertDialog.create()
        alert.setCanceledOnTouchOutside(false)
        alert.show()
    }

    private val onFilesSelectedToUpload = Observer<MutableList<Uri>> { fileUriList ->
        if (fileUriList.isNotEmpty()) {
            viewModel.uploadToCurrentFolder(fileUriList)
            fileUriList.clear()
        }
    }

    private val onDownloadFinished = Observer<Download> { download ->
        downloadsAdapter.remove(download)
    }

    private val onDownloadsRetrieved = Observer<MutableList<Download>> {
        downloadsAdapter.set(it)
    }

    private val onRecordsRetrieved = Observer<List<Record>> {
        recordsAdapter.setRecords(it)
    }

    private val onNewTemporaryFiles = Observer<MutableList<Record>> {
        recordsAdapter.addRecords(it)
    }

    private val onShowRecordSearchFragment = Observer<Void> {
        findNavController().navigate(R.id.action_publicFilesFragment_to_recordSearchFragment)
    }

    private val onShowAddOptionsFragment = Observer<NavigationFolderIdentifier> {
        addOptionsFragment = AddOptionsFragment()
        addOptionsFragment?.setBundleArguments(it, true)
        addOptionsFragment?.show(parentFragmentManager, addOptionsFragment?.tag)
        addOptionsFragment?.getOnRefreshFolder()?.observe(this, onRefreshFolder)
    }

    private val onShowRecordOptionsFragment = Observer<Record> {
        recordOptionsFragment = RecordOptionsFragment()
        recordOptionsFragment?.setBundleArguments(it, Workspace.PUBLIC_FILES)
        recordOptionsFragment?.show(parentFragmentManager, recordOptionsFragment?.tag)
        recordOptionsFragment?.getOnFileDownloadRequest()?.observe(this, onFileDownloadRequest)
        recordOptionsFragment?.getOnRecordDeleteRequest()?.observe(this, onRecordDeleteRequest)
        recordOptionsFragment?.getOnRecordRenameRequest()?.observe(this, onRecordRenameRequest)
        recordOptionsFragment?.getOnRecordRelocateRequest()?.observe(this, onRecordRelocateRequest)
    }

    private val onShowSortOptionsFragment = Observer<SortType> {
        sortOptionsFragment = SortOptionsFragment()
        sortOptionsFragment?.setBundleArguments(it)
        sortOptionsFragment?.show(parentFragmentManager, sortOptionsFragment?.tag)
        sortOptionsFragment?.getOnSortRequest()?.observe(this, onSortRequest)
    }

    private val onFileDownloadRequest = Observer<Record> {
        viewModel.download(it)
    }

    private val onRecordDeleteRequest = Observer<Record> { record ->
        val dialogBinding: DialogDeleteBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.dialog_delete, null, false
        )
        val alert = AlertDialog.Builder(context).setView(dialogBinding.root).create()

        dialogBinding.tvTitle.text = getString(R.string.delete_record_title, record.displayName)
        dialogBinding.btnDelete.setOnClickListener {
            viewModel.delete(record)
            alert.dismiss()
        }
        dialogBinding.btnCancel.setOnClickListener {
            alert.dismiss()
        }
        alert.show()
    }

    private val onRecordRenameRequest = Observer<Record> { record ->
        renameDialogBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.dialog_rename_record, null, false
        )
        renameDialogBinding.executePendingBindings()
        renameDialogBinding.lifecycleOwner = this
        renameDialogBinding.viewModel = renameDialogViewModel
        renameDialogViewModel.setRecordName(record.displayName)

        alertDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(renameDialogBinding.root).create()
        renameDialogBinding.tvTitle.text =
            getString(R.string.rename_record_title, record.displayName)
        renameDialogBinding.btnRename.setOnClickListener {
            renameDialogViewModel.renameRecord(record)
        }
        renameDialogBinding.btnCancel.setOnClickListener {
            alertDialog?.dismiss()
        }
        alertDialog?.show()
    }

    private val onCancelAllUploads = Observer<Void> {
        val dialogBinding: DialogCancelUploadsBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.dialog_cancel_uploads, null, false
        )
        val alert = AlertDialog.Builder(context).setView(dialogBinding.root).create()

        dialogBinding.btnCancelAll.setOnClickListener {
            viewModel.cancelAllUploads()
            alert.dismiss()
        }
        dialogBinding.btnNo.setOnClickListener {
            alert.dismiss()
        }
        alert.show()
    }

    private val onFileViewRequest = Observer<ArrayList<Record>> {
        val bundle = bundleOf(PARCELABLE_FILES_KEY to it)
        findNavController().navigate(R.id.action_publicFilesFragment_to_fileActivity, bundle)
    }

    private val shrinkIslandRequestObserver = Observer<Void> {
        resizeIslandWidthAnimated(binding.flFloatingActionIsland.width, ISLAND_WIDTH_SMALL)
    }

    private val expandIslandRequestObserver = Observer<Void> {
        resizeIslandWidthAnimated(binding.flFloatingActionIsland.width, ISLAND_WIDTH_LARGE)
    }

    private val deleteRecordsObserver = Observer<Void> {
        val dialogBinding: DialogDeleteBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.dialog_delete, null, false
        )
        val alert = AlertDialog.Builder(context).setView(dialogBinding.root).create()

        dialogBinding.tvTitle.text = getString(R.string.delete_records_title)
        dialogBinding.btnDelete.setOnClickListener {
            viewModel.deleteSelectedRecords()
            alert.dismiss()
        }
        dialogBinding.btnCancel.setOnClickListener {
            alert.dismiss()
        }
        alert.show()
    }

    private val refreshCurrentFolderObserver = Observer<Void> {
        viewModel.refreshCurrentFolder()
    }

    private val showSelectionOptionsObserver = Observer<Int> {
        selectionOptionsFragment = SelectionOptionsFragment()
        selectionOptionsFragment?.setBundleArguments(it)
        selectionOptionsFragment?.show(parentFragmentManager, selectionOptionsFragment?.tag)
        selectionOptionsFragment?.getOnSelectionRelocateRequest()?.observe(this, onSelectionRelocateObserver)
    }

    private val onSelectionRelocateObserver = Observer<RelocationType> {
        viewModel.onSelectionRelocationBtnClick(it)
    }

    private val onRecordRelocateRequest = Observer<Pair<Record, RelocationType>> {
        viewModel.setRelocationMode(it)
        lifecycleScope.launch {
            delay(DELAY_TO_RESIZE_MILLIS)
            resizeIslandWidthAnimated(binding.flFloatingActionIsland.width, ISLAND_WIDTH_LARGE)
        }
    }

    private fun resizeIslandWidthAnimated(currentWidth: Int, newWidth: Int) {
        val widthAnimator = ValueAnimator.ofInt(currentWidth, newWidth)
        widthAnimator.duration = RESIZE_DURATION_MILLIS
        widthAnimator.interpolator = DecelerateInterpolator()
        widthAnimator.addUpdateListener { animation ->
            binding.flFloatingActionIsland.layoutParams.width = animation.animatedValue as Int
            binding.flFloatingActionIsland.requestLayout()
        }
        widthAnimator.start()
    }

    private val onSortRequest = Observer<SortType> {
        viewModel.setSortType(it)
    }

    private val onRefreshFolder = Observer<Void> {
        viewModel.refreshCurrentFolder()
    }

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

    private val onRecordRenamed = Observer<Void> {
        viewModel.refreshCurrentFolder()
        alertDialog?.dismiss()
    }

    private val onRecordSelectedObserver = Observer<Record> {
        onRecordSelectedEvent.value = Pair(Workspace.PUBLIC_FILES, it)
    }

    private val onRootFolderReadyObserver = Observer<Void> {
        arguments?.let { args ->
            args.getParcelable<Record>(PARCELABLE_RECORD_KEY)?.let { folderRecord ->
                viewModel.onRecordClick(folderRecord)
            }
            args.getParcelableArrayList<Uri>(PARCELABLE_FILES_KEY)?.toList()?.let { uris ->
                viewModel.uploadToCurrentFolder(uris)
            }
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

    private fun initFilesRecyclerView(rvFiles: RecyclerView) {
        recordsRecyclerView = rvFiles
        recordsListAdapter = RecordsListAdapter(
            this,
            false,
            viewModel.getIsRelocationMode(),
            viewModel.getIsSelectionMode(),
            isForSharesScreen = false,
            isForSearchScreen = false,
            recordListener = viewModel
        )
        recordsGridAdapter = RecordsGridAdapter(
            this,
            false,
            viewModel.getIsRelocationMode(),
            viewModel.getIsSelectionMode(),
            MutableLiveData(PreviewState.ACCESS_GRANTED),
            isForSharePreviewScreen = false,
            isForSharesScreen = false,
            recordListener = viewModel
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
            setHasFixedSize(true)
        }
    }

    fun getOnRecordSelected(): MutableLiveData<Pair<Workspace, Record>> = onRecordSelectedEvent

    override fun connectViewModelEvents() {
        viewModel.getOnShowMessage().observe(this, onShowMessage)
        viewModel.getOnShowQuotaExceeded().observe(this, onShowQuotaExceeded)
        viewModel.getOnChangeViewMode().observe(this, onChangeViewMode)
        viewModel.getOnDownloadsRetrieved().observe(this, onDownloadsRetrieved)
        viewModel.getOnDownloadFinished().observe(this, onDownloadFinished)
        viewModel.getOnRecordsRetrieved().observe(this, onRecordsRetrieved)
        viewModel.getOnNewTemporaryFiles().observe(this, onNewTemporaryFiles)
        viewModel.getOnShowAddOptionsFragment().observe(this, onShowAddOptionsFragment)
        viewModel.getOnShowRecordOptionsFragment().observe(this, onShowRecordOptionsFragment)
        viewModel.getOnShowRecordSearchFragment().observe(this, onShowRecordSearchFragment)
        viewModel.getOnShowSortOptionsFragment().observe(this, onShowSortOptionsFragment)
        viewModel.getOnRecordDeleteRequest().observe(this, onRecordDeleteRequest)
        viewModel.getOnCancelAllUploads().observe(this, onCancelAllUploads)
        viewModel.getOnFileViewRequest().observe(this, onFileViewRequest)
        viewModel.getShrinkIslandRequest().observe(this, shrinkIslandRequestObserver)
        viewModel.getOnRecordSelected().observe(this, onRecordSelectedObserver)
        viewModel.getOnRootFolderReady().observe(this, onRootFolderReadyObserver)
        viewModel.getExpandIslandRequest().observe(this, expandIslandRequestObserver)
        viewModel.getDeleteRecordsRequest().observe(this, deleteRecordsObserver)
        viewModel.getRefreshCurrentFolderRequest().observe(this, refreshCurrentFolderObserver)
        viewModel.getShowSelectionOptionsRequest().observe(this, showSelectionOptionsObserver)
        renameDialogViewModel.getOnRecordRenamed().observe(this, onRecordRenamed)
        renameDialogViewModel.getOnShowMessage().observe(this, onShowMessage)
        addOptionsFragment?.getOnFilesSelected()?.observe(this, onFilesSelectedToUpload)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnShowMessage().removeObserver(onShowMessage)
        viewModel.getOnShowQuotaExceeded().removeObserver(onShowQuotaExceeded)
        viewModel.getOnChangeViewMode().removeObserver(onChangeViewMode)
        viewModel.getOnDownloadsRetrieved().removeObserver(onDownloadsRetrieved)
        viewModel.getOnDownloadFinished().removeObserver(onDownloadFinished)
        viewModel.getOnRecordsRetrieved().removeObserver(onRecordsRetrieved)
        viewModel.getOnNewTemporaryFiles().removeObserver(onNewTemporaryFiles)
        viewModel.getOnShowAddOptionsFragment().removeObserver(onShowAddOptionsFragment)
        viewModel.getOnShowRecordOptionsFragment().removeObserver(onShowRecordOptionsFragment)
        viewModel.getOnShowRecordSearchFragment().removeObserver(onShowRecordSearchFragment)
        viewModel.getOnShowSortOptionsFragment().removeObserver(onShowSortOptionsFragment)
        viewModel.getOnRecordDeleteRequest().removeObserver(onRecordDeleteRequest)
        viewModel.getOnCancelAllUploads().removeObserver(onCancelAllUploads)
        viewModel.getOnFileViewRequest().removeObserver(onFileViewRequest)
        viewModel.getShrinkIslandRequest().removeObserver(shrinkIslandRequestObserver)
        viewModel.getOnRecordSelected().removeObserver(onRecordSelectedObserver)
        viewModel.getOnRootFolderReady().removeObserver(onRootFolderReadyObserver)
        viewModel.getExpandIslandRequest().removeObserver(expandIslandRequestObserver)
        viewModel.getDeleteRecordsRequest().removeObserver(deleteRecordsObserver)
        viewModel.getRefreshCurrentFolderRequest().removeObserver(refreshCurrentFolderObserver)
        viewModel.getShowSelectionOptionsRequest().removeObserver(showSelectionOptionsObserver)
        renameDialogViewModel.getOnRecordRenamed().removeObserver(onRecordRenamed)
        renameDialogViewModel.getOnShowMessage().removeObserver(onShowMessage)
        addOptionsFragment?.getOnFilesSelected()?.removeObserver(onFilesSelectedToUpload)
        addOptionsFragment?.getOnRefreshFolder()?.removeObserver(onRefreshFolder)
        recordOptionsFragment?.getOnFileDownloadRequest()?.removeObserver(onFileDownloadRequest)
        recordOptionsFragment?.getOnRecordDeleteRequest()?.removeObserver(onRecordDeleteRequest)
        recordOptionsFragment?.getOnRecordRenameRequest()?.removeObserver(onRecordRenameRequest)
        recordOptionsFragment?.getOnRecordRelocateRequest()?.removeObserver(onRecordRelocateRequest)
        sortOptionsFragment?.getOnSortRequest()?.removeObserver(onSortRequest)
        selectionOptionsFragment?.getOnSelectionRelocateRequest()?.removeObserver(onSelectionRelocateObserver)
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
        if (shouldRefreshCurrentFolder) viewModel.refreshCurrentFolder()
        shouldRefreshCurrentFolder = true
        binding.root.windowToken?.let { context?.hideKeyboardFrom(binding.root.windowToken) }
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }
}