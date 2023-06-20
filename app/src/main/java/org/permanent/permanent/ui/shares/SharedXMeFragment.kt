package org.permanent.permanent.ui.shares

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
import androidx.lifecycle.LiveData
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
import org.permanent.permanent.databinding.DialogTitleTextTwoButtonsBinding
import org.permanent.permanent.databinding.FragmentSharedXMeBinding
import org.permanent.permanent.models.Download
import org.permanent.permanent.models.NavigationFolderIdentifier
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.SelectionOptionsFragment
import org.permanent.permanent.ui.Workspace
import org.permanent.permanent.ui.myFiles.AddOptionsFragment
import org.permanent.permanent.ui.myFiles.MyFilesFragment
import org.permanent.permanent.ui.myFiles.PARCELABLE_FILES_KEY
import org.permanent.permanent.ui.myFiles.RecordOptionsFragment
import org.permanent.permanent.ui.myFiles.RecordsAdapter
import org.permanent.permanent.ui.myFiles.RecordsGridAdapter
import org.permanent.permanent.ui.myFiles.RecordsListAdapter
import org.permanent.permanent.ui.myFiles.RelocationType
import org.permanent.permanent.ui.myFiles.SortOptionsFragment
import org.permanent.permanent.ui.myFiles.SortType
import org.permanent.permanent.ui.myFiles.download.DownloadsAdapter
import org.permanent.permanent.viewmodels.RenameRecordViewModel
import org.permanent.permanent.viewmodels.SharedXMeViewModel
import org.permanent.permanent.viewmodels.SingleLiveEvent

class SharedXMeFragment : PermanentBaseFragment() {

    private lateinit var viewModel: SharedXMeViewModel
    private lateinit var binding: FragmentSharedXMeBinding
    private lateinit var downloadsRecyclerView: RecyclerView
    private lateinit var downloadsAdapter: DownloadsAdapter
    private lateinit var recordsRecyclerView: RecyclerView
    private lateinit var recordsAdapter: RecordsAdapter
    private lateinit var recordsListAdapter: RecordsListAdapter
    private lateinit var recordsGridAdapter: RecordsGridAdapter
    private lateinit var renameDialogViewModel: RenameRecordViewModel
    private lateinit var renameDialogBinding: DialogRenameRecordBinding
    private var alertDialog: androidx.appcompat.app.AlertDialog? = null
    private lateinit var record: Record
    private lateinit var prefsHelper: PreferencesHelper
    private var isSharedWithMeFragment = false
    private val getRootRecords = SingleLiveEvent<Void>()
    private var addOptionsFragment: AddOptionsFragment? = null
    private var recordOptionsFragment: RecordOptionsFragment? = null
    private var sortOptionsFragment: SortOptionsFragment? = null
    private var selectionOptionsFragment: SelectionOptionsFragment? = null
    private val onRecordSelectedEvent = SingleLiveEvent<Record>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        renameDialogViewModel = ViewModelProvider(this)[RenameRecordViewModel::class.java]
        viewModel = ViewModelProvider(this)[SharedXMeViewModel::class.java]
        binding = FragmentSharedXMeBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        viewModel.setLifecycleOwner(this)
        prefsHelper = PreferencesHelper(
            requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        )
        viewModel.initUploadsRecyclerView(binding.rvUploads, this)
        initDownloadsRecyclerView(binding.rvDownloads)
        initRecordsRecyclerView(binding.rvShares)
        arguments?.takeIf { it.containsKey(SHARED_X_ME_NO_ITEMS_MESSAGE_KEY) }?.apply {
            getString(SHARED_X_ME_NO_ITEMS_MESSAGE_KEY).also { binding.tvNoShares.text = it }
        }
        arguments?.takeIf { it.containsKey(SHARED_WITH_ME_ITEM_LIST_KEY) }?.apply {
            isSharedWithMeFragment = true
            getParcelableArrayList<Record>(SHARED_WITH_ME_ITEM_LIST_KEY).also {
                if (!it.isNullOrEmpty()) setShares(it)
            }
        }
        arguments?.takeIf { it.containsKey(SHOW_SCREEN_SIMPLIFIED_KEY) }?.apply {
            val showScreenSimplified = getBoolean(SHOW_SCREEN_SIMPLIFIED_KEY)
            if (showScreenSimplified) viewModel.setShowScreenSimplified()
        }
        return binding.root
    }

    private val onShowMessage = Observer<String> { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
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

    private val onShowAddOptionsFragment = Observer<NavigationFolderIdentifier> {
        addOptionsFragment = AddOptionsFragment()
        addOptionsFragment?.setBundleArguments(it, false)
        addOptionsFragment?.show(parentFragmentManager, addOptionsFragment?.tag)
        addOptionsFragment?.getOnRefreshFolder()?.observe(this, onRefreshFolder)
    }

    private val onShowRecordOptionsFragment = Observer<Record> {
        this.record = it
        recordOptionsFragment = RecordOptionsFragment()
        recordOptionsFragment?.setBundleArguments(
            record, Workspace.SHARES, isSharedWithMeFragment, viewModel.isRoot.value ?: false
        )
        recordOptionsFragment?.show(parentFragmentManager, recordOptionsFragment?.tag)
        recordOptionsFragment?.getOnFileDownloadRequest()?.observe(this, onFileDownloadRequest)
        recordOptionsFragment?.getOnRecordDeleteRequest()?.observe(this, onRecordDeleteRequest)
        recordOptionsFragment?.getOnRecordLeaveShareRequest()
            ?.observe(this, onRecordLeaveShareRequest)
        recordOptionsFragment?.getOnRecordRenameRequest()?.observe(this, onRecordRenameRequest)
        recordOptionsFragment?.getOnRecordRelocateRequest()?.observe(this, onRecordRelocateObserver)
    }

    private val onRefreshFolder = Observer<Void> {
        viewModel.refreshCurrentFolder()
    }

    private val onFilesSelectedToUpload = Observer<MutableList<Uri>> { fileUriList ->
        if (fileUriList.isNotEmpty()) {
            viewModel.upload(fileUriList)
            fileUriList.clear()
        }
    }

    fun uploadFilesToFolder(folder: Record?, uris: ArrayList<Uri>?) {
        uris?.let { viewModel.uploadFilesToFolder(folder, it) }
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

    private val onNewTemporaryFiles = Observer<MutableList<Record>> {
        recordsAdapter.addRecords(it)
    }

    private val onRootSharesNeeded = Observer<Void> { getRootRecords.call() }

    private val onChangeViewMode = Observer<Boolean> { isListViewMode ->
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

    private val onRecordLeaveShareRequest = Observer<Record> { record ->
        val dialogBinding: DialogDeleteBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.dialog_delete, null, false
        )
        val alert = AlertDialog.Builder(context).setView(dialogBinding.root).create()

        dialogBinding.tvTitle.text = getString(R.string.leave_share_record_title, record.displayName)
        dialogBinding.btnDelete.text = getString(R.string.button_leave_share)
        dialogBinding.btnDelete.setOnClickListener {
            viewModel.unshare(record)
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

    private val onRecordRenamed = Observer<Void> {
        viewModel.refreshCurrentFolder()
        alertDialog?.dismiss()
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

    private val onFileViewRequest = Observer<Record> {
        val files = ArrayList<Record>()
        files.add(it)
        val bundle = bundleOf(PARCELABLE_FILES_KEY to files)
        requireParentFragment().findNavController()
            .navigate(R.id.action_sharesFragment_to_fileActivity, bundle)
    }

    private val relocationCancellationObserver = Observer<Void> {
        val dialogBinding: DialogTitleTextTwoButtonsBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.dialog_title_text_two_buttons, null, false
        )
        val alert = AlertDialog.Builder(context).setView(dialogBinding.root).create()

        dialogBinding.tvTitle.text = getString(R.string.dialog_shared_files_cancel_move_title)
        dialogBinding.tvText.text = getString(R.string.dialog_shared_files_cancel_move_text)
        dialogBinding.btnPositive.text = getString(R.string.button_cancel_move)
        dialogBinding.btnPositive.setOnClickListener {
            viewModel.cancelRelocationMode()
            viewModel.navigateBack()
            alert.dismiss()
        }
        dialogBinding.btnNegative.text = getString(R.string.button_continue)
        dialogBinding.btnNegative.setOnClickListener {
            alert.dismiss()
        }
        alert.show()
    }

    private val onFileDownloadRequest = Observer<Record> {
        viewModel.download(it)
    }

    private val shrinkIslandRequestObserver = Observer<Void> {
        resizeIslandWidthAnimated(
            binding.flFloatingActionIsland.width, MyFilesFragment.ISLAND_WIDTH_SMALL
        )
    }

    private val expandIslandRequestObserver = Observer<Void> {
        resizeIslandWidthAnimated(
            binding.flFloatingActionIsland.width,
            MyFilesFragment.ISLAND_WIDTH_LARGE
        )
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

    private val onRecordSelectedObserver = Observer<Record> {
        onRecordSelectedEvent.value = it
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

    private val onRecordRelocateObserver = Observer<Pair<Record, RelocationType>> {
        viewModel.setRelocationMode(Pair(mutableListOf(it.first), it.second))
        lifecycleScope.launch {
            delay(MyFilesFragment.DELAY_TO_RESIZE_MILLIS)
            resizeIslandWidthAnimated(
                binding.flFloatingActionIsland.width, MyFilesFragment.ISLAND_WIDTH_LARGE
            )
        }
    }

    private fun resizeIslandWidthAnimated(currentWidth: Int, newWidth: Int) {
        val widthAnimator = ValueAnimator.ofInt(currentWidth, newWidth)
        widthAnimator.duration = MyFilesFragment.RESIZE_DURATION_MILLIS
        widthAnimator.interpolator = DecelerateInterpolator()
        widthAnimator.addUpdateListener { animation ->
            binding.flFloatingActionIsland.layoutParams.width = animation.animatedValue as Int
            binding.flFloatingActionIsland.requestLayout()
        }
        widthAnimator.start()
    }

    private fun initRecordsRecyclerView(rvRecords: RecyclerView) {
        recordsRecyclerView = rvRecords
        recordsListAdapter = RecordsListAdapter(
            this,
            false,
            viewModel.getIsRelocationMode(),
            viewModel.getIsSelectionMode(),
            isForSharesScreen = true,
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
            isForSharesScreen = true,
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
        viewModel.isRoot.value = true
        viewModel.existsFiles.value = true
    }

    fun navigateToRecord(recordIdToNavigateTo: Int) {
        recordsAdapter.getItemById(recordIdToNavigateTo)?.let { record ->
            viewModel.onRecordClick(record)
        }
    }

    fun getRootShares(): LiveData<Void> = getRootRecords

    private val onShowSortOptionsFragment = Observer<SortType> {
        sortOptionsFragment = SortOptionsFragment()
        sortOptionsFragment?.setBundleArguments(it)
        sortOptionsFragment?.show(parentFragmentManager, recordOptionsFragment?.tag)
        sortOptionsFragment?.getOnSortRequest()?.observe(this, onSortRequest)
    }

    private val onSortRequest = Observer<SortType> {
        viewModel.setSortType(it)
    }

    private fun checkForViewModeChange() {
        val isListViewMode = prefsHelper.isListViewMode()
        viewModel.getIsListViewMode().value = isListViewMode
        onChangeViewMode.onChanged(isListViewMode)
    }

    fun getOnRecordSelected(): MutableLiveData<Record> = onRecordSelectedEvent

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onShowMessage)
        viewModel.getOnShowQuotaExceeded().observe(this, onShowQuotaExceeded)
        viewModel.getOnShowAddOptionsFragment().observe(this, onShowAddOptionsFragment)
        viewModel.getOnShowRecordOptionsFragment().observe(this, onShowRecordOptionsFragment)
        viewModel.getOnDownloadsRetrieved().observe(this, onDownloadsRetrieved)
        viewModel.getOnDownloadFinished().observe(this, onDownloadFinished)
        viewModel.getOnRecordsRetrieved().observe(this, onRecordsRetrieved)
        viewModel.getOnNewTemporaryFiles().observe(this, onNewTemporaryFiles)
        viewModel.getOnRootSharesNeeded().observe(this, onRootSharesNeeded)
        viewModel.getOnFileViewRequest().observe(this, onFileViewRequest)
        viewModel.getShowRelocationCancellationDialog()
            .observe(this, relocationCancellationObserver)
        viewModel.getOnShowSortOptionsFragment().observe(this, onShowSortOptionsFragment)
        viewModel.getOnCancelAllUploads().observe(this, onCancelAllUploads)
        viewModel.getOnChangeViewMode().observe(this, onChangeViewMode)
        viewModel.getOnRecordSelected().observe(this, onRecordSelectedObserver)
        viewModel.getShrinkIslandRequest().observe(this, shrinkIslandRequestObserver)
        viewModel.getExpandIslandRequest().observe(this, expandIslandRequestObserver)
        viewModel.getDeleteRecordsRequest().observe(this, deleteRecordsObserver)
        viewModel.getRefreshCurrentFolderRequest().observe(this, refreshCurrentFolderObserver)
        viewModel.getShowSelectionOptionsRequest().observe(this, showSelectionOptionsObserver)
        renameDialogViewModel.getOnRecordRenamed().observe(this, onRecordRenamed)
        renameDialogViewModel.getOnShowMessage().observe(this, onShowMessage)
        addOptionsFragment?.getOnFilesSelected()?.observe(this, onFilesSelectedToUpload)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getOnShowQuotaExceeded().removeObserver(onShowQuotaExceeded)
        viewModel.getOnShowAddOptionsFragment().removeObserver(onShowAddOptionsFragment)
        viewModel.getOnShowRecordOptionsFragment().removeObserver(onShowRecordOptionsFragment)
        viewModel.getOnDownloadsRetrieved().removeObserver(onDownloadsRetrieved)
        viewModel.getOnDownloadFinished().removeObserver(onDownloadFinished)
        viewModel.getOnRecordsRetrieved().removeObserver(onRecordsRetrieved)
        viewModel.getOnNewTemporaryFiles().removeObserver(onNewTemporaryFiles)
        viewModel.getOnRootSharesNeeded().removeObserver(onRootSharesNeeded)
        viewModel.getOnFileViewRequest().removeObserver(onFileViewRequest)
        viewModel.getShowRelocationCancellationDialog()
            .removeObserver(relocationCancellationObserver)
        viewModel.getOnShowSortOptionsFragment().removeObserver(onShowSortOptionsFragment)
        viewModel.getOnCancelAllUploads().removeObserver(onCancelAllUploads)
        viewModel.getOnChangeViewMode().removeObserver(onChangeViewMode)
        viewModel.getOnRecordSelected().removeObserver(onRecordSelectedObserver)
        viewModel.getShrinkIslandRequest().removeObserver(shrinkIslandRequestObserver)
        viewModel.getExpandIslandRequest().removeObserver(expandIslandRequestObserver)
        viewModel.getDeleteRecordsRequest().removeObserver(deleteRecordsObserver)
        viewModel.getRefreshCurrentFolderRequest().removeObserver(refreshCurrentFolderObserver)
        viewModel.getShowSelectionOptionsRequest().removeObserver(showSelectionOptionsObserver)
        recordOptionsFragment?.getOnFileDownloadRequest()?.removeObserver(onFileDownloadRequest)
        recordOptionsFragment?.getOnRecordRenameRequest()?.removeObserver(onRecordRenameRequest)
        recordOptionsFragment?.getOnRecordDeleteRequest()?.removeObserver(onRecordDeleteRequest)
        recordOptionsFragment?.getOnRecordRelocateRequest()?.removeObserver(onRecordRelocateObserver)
        renameDialogViewModel.getOnRecordRenamed().removeObserver(onRecordRenamed)
        renameDialogViewModel.getOnShowMessage().removeObserver(onShowMessage)
        sortOptionsFragment?.getOnSortRequest()?.removeObserver(onSortRequest)
        addOptionsFragment?.getOnFilesSelected()?.removeObserver(onFilesSelectedToUpload)
        selectionOptionsFragment?.getOnSelectionRelocateRequest()?.removeObserver(onSelectionRelocateObserver)
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
        checkForViewModeChange()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }
}