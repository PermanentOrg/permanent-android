package org.permanent.permanent.ui.shares

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.dialog_cancel_uploads.view.*
import kotlinx.android.synthetic.main.dialog_cancel_uploads.view.tvText
import kotlinx.android.synthetic.main.dialog_cancel_uploads.view.tvTitle
import kotlinx.android.synthetic.main.dialog_delete.view.*
import kotlinx.android.synthetic.main.dialog_title_text_two_buttons.view.*
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.R
import org.permanent.permanent.databinding.DialogRenameRecordBinding
import org.permanent.permanent.databinding.FragmentSharedXMeBinding
import org.permanent.permanent.models.Download
import org.permanent.permanent.models.NavigationFolderIdentifier
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.Workspace
import org.permanent.permanent.ui.myFiles.*
import org.permanent.permanent.ui.myFiles.download.DownloadsAdapter
import org.permanent.permanent.ui.shareManagement.ShareManagementFragment
import org.permanent.permanent.viewmodels.RenameRecordViewModel
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
    private var shareManagementFragment: ShareManagementFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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
                if (!it.isNullOrEmpty()) recordsAdapter.setRecords(it)
                viewModel.existsShares.value = !it.isNullOrEmpty()
            }
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

    private val onRefreshFolder = Observer<Void> {
        viewModel.refreshCurrentFolder()
    }

    private val onFilesSelectedToUpload = Observer<MutableList<Uri>> { fileUriList ->
        if (fileUriList.isNotEmpty()) {
            viewModel.upload(fileUriList)
            fileUriList.clear()
        }
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

    private val onNewTemporaryFile = Observer<Record> {
        recordsAdapter.addRecord(it)
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
        val viewDialog: View = layoutInflater.inflate(R.layout.dialog_delete, null)
        val alert = AlertDialog.Builder(context)
            .setView(viewDialog)
            .create()
        viewDialog.tvTitle.text = getString(R.string.delete_record_title, record.displayName)
        viewDialog.btnDelete.setOnClickListener {
            viewModel.delete(record)
            alert.dismiss()
        }
        viewDialog.btnCancel.setOnClickListener {
            alert.dismiss()
        }
        alert.show()
    }

    private val onRecordLeaveShareRequest = Observer<Record> { record ->
        val viewDialog: View = layoutInflater.inflate(R.layout.dialog_delete, null)
        val alert = AlertDialog.Builder(context)
            .setView(viewDialog)
            .create()
        viewDialog.tvTitle.text = getString(R.string.leave_share_record_title, record.displayName)
        viewDialog.btnDelete.text = getString(R.string.button_leave_share)
        viewDialog.btnDelete.setOnClickListener {
            viewModel.unshare(record)
            alert.dismiss()
        }
        viewDialog.btnCancel.setOnClickListener {
            alert.dismiss()
        }
        alert.show()
    }

    private val onRecordRenameRequest = Observer<Record> { record ->
        renameDialogBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_rename_record, null, false
        )
        renameDialogBinding.executePendingBindings()
        renameDialogBinding.lifecycleOwner = this
        renameDialogBinding.viewModel = renameDialogViewModel
        renameDialogViewModel.setRecordName(record.displayName)

        alertDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(renameDialogBinding.root)
            .create()
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

    private val onRecordRelocateRequest = Observer<Pair<Record, RelocationType>> {
        viewModel.setRelocationMode(it)
    }

    private val onRecordManageSharingObserver = Observer<Record> {
        showShareManagementFragment(it)
    }

    private val onRecordRenamed = Observer<Void> {
        viewModel.refreshCurrentFolder()
        alertDialog?.dismiss()
    }

    private val onCancelAllUploads = Observer<Void> {
        val viewDialog: View = layoutInflater.inflate(R.layout.dialog_cancel_uploads, null)
        val alert = AlertDialog.Builder(context)
            .setView(viewDialog)
            .create()
        viewDialog.btnCancelAll.setOnClickListener {
            viewModel.cancelAllUploads()
            alert.dismiss()
        }
        viewDialog.btnNo.setOnClickListener {
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
        val viewDialog: View = layoutInflater.inflate(R.layout.dialog_title_text_two_buttons, null)
        val alert = AlertDialog.Builder(context).setView(viewDialog).create()

        viewDialog.tvTitle.text = getString(R.string.dialog_shared_files_cancel_move_title)
        viewDialog.tvText.text = getString(R.string.dialog_shared_files_cancel_move_text)
        viewDialog.btnPositive.text = getString(R.string.button_cancel_move)
        viewDialog.btnPositive.setOnClickListener {
            viewModel.cancelRelocationMode()
            viewModel.navigateBack()
            alert.dismiss()
        }
        viewDialog.btnNegative.text = getString(R.string.button_continue)
        viewDialog.btnNegative.setOnClickListener {
            alert.dismiss()
        }
        alert.show()
    }

    private val onFileDownloadRequest = Observer<Record> {
        viewModel.download(it)
    }

    private fun initRecordsRecyclerView(rvRecords: RecyclerView) {
        recordsRecyclerView = rvRecords
        recordsListAdapter = RecordsListAdapter(
            this, false,
            viewModel.getIsRelocationMode(),
            isForSharesScreen = true,
            isForSearchScreen = false,
            recordListener = this
        )
        recordsGridAdapter = RecordsGridAdapter(
            this, false,
            viewModel.getIsRelocationMode(),
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
        viewModel.existsShares.value = true
        viewModel.showEmptyFolder.value = false
    }

    private fun showShareManagementFragment(record: Record?) {
        shareManagementFragment = ShareManagementFragment()
        shareManagementFragment?.setBundleArguments(record)
        shareManagementFragment?.show(parentFragmentManager, shareManagementFragment?.tag)
    }

    fun navigateToRecord(recordIdToNavigateTo: Int) {
        recordsAdapter.getItemById(recordIdToNavigateTo)?.let { record ->
            viewModel.onRecordClick(record)
        }
    }

    fun getRootShares(): LiveData<Void> = getRootRecords

    override fun onRecordClick(record: Record) {
        viewModel.onRecordClick(record)
    }

    override fun onRecordOptionsClick(record: Record) {
        this.record = record
        recordOptionsFragment = RecordOptionsFragment()
        recordOptionsFragment?.setBundleArguments(
            record,
            Workspace.SHARES,
            isSharedWithMeFragment,
            viewModel.isRoot.value ?: false
        )
        recordOptionsFragment?.show(parentFragmentManager, recordOptionsFragment?.tag)
        recordOptionsFragment?.getOnFileDownloadRequest()?.observe(this, onFileDownloadRequest)
        recordOptionsFragment?.getOnRecordDeleteRequest()?.observe(this, onRecordDeleteRequest)
        recordOptionsFragment?.getOnRecordLeaveShareRequest()?.observe(this, onRecordLeaveShareRequest)
        recordOptionsFragment?.getOnRecordRenameRequest()?.observe(this, onRecordRenameRequest)
        recordOptionsFragment?.getOnRecordRelocateRequest()?.observe(this, onRecordRelocateRequest)
        recordOptionsFragment?.getOnRecordManageSharingRequest()
            ?.observe(this, onRecordManageSharingObserver)
    }

    private val onShowSortOptionsFragment = Observer<SortType> {
        sortOptionsFragment = SortOptionsFragment()
        sortOptionsFragment?.setBundleArguments(it)
        sortOptionsFragment?.show(parentFragmentManager, recordOptionsFragment?.tag)
        sortOptionsFragment?.getOnSortRequest()?.observe(this, onSortRequest)
    }

    private val onSortRequest = Observer<SortType> {
        viewModel.setSortType(it)
    }

    override fun onRecordDeleteClick(record: Record) {}

    private fun checkForViewModeChange() {
        val isListViewMode = prefsHelper.isListViewMode()
        viewModel.getIsListViewMode().value = isListViewMode
        onChangeViewMode.onChanged(isListViewMode)
    }

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onShowMessage)
        viewModel.getOnShowQuotaExceeded().observe(this, onShowQuotaExceeded)
        viewModel.getOnShowAddOptionsFragment().observe(this, onShowAddOptionsFragment)
        viewModel.getOnDownloadsRetrieved().observe(this, onDownloadsRetrieved)
        viewModel.getOnDownloadFinished().observe(this, onDownloadFinished)
        viewModel.getOnRecordsRetrieved().observe(this, onRecordsRetrieved)
        viewModel.getOnNewTemporaryFile().observe(this, onNewTemporaryFile)
        viewModel.getOnRootSharesNeeded().observe(this, onRootSharesNeeded)
        viewModel.getOnFileViewRequest().observe(this, onFileViewRequest)
        viewModel.getShowRelocationCancellationDialog()
            .observe(this, relocationCancellationObserver)
        viewModel.getOnShowSortOptionsFragment().observe(this, onShowSortOptionsFragment)
        viewModel.getOnCancelAllUploads().observe(this, onCancelAllUploads)
        viewModel.getOnChangeViewMode().observe(this, onChangeViewMode)
        renameDialogViewModel.getOnRecordRenamed().observe(this, onRecordRenamed)
        renameDialogViewModel.getOnShowMessage().observe(this, onShowMessage)
        addOptionsFragment?.getOnFilesSelected()?.observe(this, onFilesSelectedToUpload)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getOnShowQuotaExceeded().removeObserver(onShowQuotaExceeded)
        viewModel.getOnShowAddOptionsFragment().removeObserver(onShowAddOptionsFragment)
        viewModel.getOnDownloadsRetrieved().removeObserver(onDownloadsRetrieved)
        viewModel.getOnDownloadFinished().removeObserver(onDownloadFinished)
        viewModel.getOnRecordsRetrieved().removeObserver(onRecordsRetrieved)
        viewModel.getOnNewTemporaryFile().removeObserver(onNewTemporaryFile)
        viewModel.getOnRootSharesNeeded().removeObserver(onRootSharesNeeded)
        viewModel.getOnFileViewRequest().removeObserver(onFileViewRequest)
        viewModel.getShowRelocationCancellationDialog()
            .removeObserver(relocationCancellationObserver)
        viewModel.getOnShowSortOptionsFragment().removeObserver(onShowSortOptionsFragment)
        viewModel.getOnCancelAllUploads().removeObserver(onCancelAllUploads)
        viewModel.getOnChangeViewMode().removeObserver(onChangeViewMode)
        recordOptionsFragment?.getOnFileDownloadRequest()?.removeObserver(onFileDownloadRequest)
        recordOptionsFragment?.getOnRecordRenameRequest()?.removeObserver(onRecordRenameRequest)
        recordOptionsFragment?.getOnRecordDeleteRequest()?.removeObserver(onRecordDeleteRequest)
        recordOptionsFragment?.getOnRecordRelocateRequest()?.removeObserver(onRecordRelocateRequest)
        recordOptionsFragment?.getOnRecordManageSharingRequest()
            ?.removeObserver(onRecordManageSharingObserver)
        renameDialogViewModel.getOnRecordRenamed().removeObserver(onRecordRenamed)
        renameDialogViewModel.getOnShowMessage().removeObserver(onShowMessage)
        sortOptionsFragment?.getOnSortRequest()?.removeObserver(onSortRequest)
        addOptionsFragment?.getOnFilesSelected()?.removeObserver(onFilesSelectedToUpload)
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