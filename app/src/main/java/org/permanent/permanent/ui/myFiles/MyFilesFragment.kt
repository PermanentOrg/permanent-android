package org.permanent.permanent.ui.myFiles

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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.dialog_cancel_uploads.view.*
import kotlinx.android.synthetic.main.dialog_delete.view.*
import kotlinx.android.synthetic.main.dialog_delete.view.tvTitle
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.R
import org.permanent.permanent.databinding.DialogRenameRecordBinding
import org.permanent.permanent.databinding.FragmentMyFilesBinding
import org.permanent.permanent.models.Download
import org.permanent.permanent.models.NavigationFolderIdentifier
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.hideKeyboardFrom
import org.permanent.permanent.ui.myFiles.download.DownloadsAdapter
import org.permanent.permanent.ui.shares.PreviewState
import org.permanent.permanent.ui.shares.SHOW_SCREEN_SIMPLIFIED_KEY
import org.permanent.permanent.ui.shares.URL_TOKEN_KEY
import org.permanent.permanent.viewmodels.MyFilesViewModel
import org.permanent.permanent.viewmodels.RenameRecordViewModel
import org.permanent.permanent.viewmodels.SingleLiveEvent

const val PARCELABLE_RECORD_KEY = "parcelable_record_key"
const val PARCELABLE_FILES_KEY = "parcelable_files_key"

class MyFilesFragment : PermanentBaseFragment() {
    private lateinit var binding: FragmentMyFilesBinding
    private lateinit var viewModel: MyFilesViewModel
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
    private var addOptionsFragment: AddOptionsFragment? = null
    private var recordOptionsFragment: RecordOptionsFragment? = null
    private var sortOptionsFragment: SortOptionsFragment? = null
    private val onPhotoSelectedEvent = SingleLiveEvent<Record>()
    private var shouldRefreshCurrentFolder = false
    private var showScreenSimplified = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyFilesBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(MyFilesViewModel::class.java)
        renameDialogViewModel = ViewModelProvider(this).get(RenameRecordViewModel::class.java)

        val record: Record? = arguments?.getParcelable(PARCELABLE_RECORD_KEY)
        if (record != null) {
            // notification deeplink
            navigateToShareLinkFragment(record)
            arguments?.clear()
        } else {
            prefsHelper = PreferencesHelper(
                requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            )
            val shareLinkUrlToken = prefsHelper.getShareLinkUrlToken()

            if (!shareLinkUrlToken.isNullOrEmpty()) {
                // click on shareLinkUrl not consumed
                prefsHelper.saveShareLinkUrlToken("")
                navigateToSharePreviewFragment(shareLinkUrlToken)
            } else {
                binding.executePendingBindings()
                binding.lifecycleOwner = this
                binding.viewModel = viewModel
                arguments?.takeIf { it.containsKey(SHOW_SCREEN_SIMPLIFIED_KEY) }?.apply {
                    showScreenSimplified = getBoolean(SHOW_SCREEN_SIMPLIFIED_KEY)
                    if (showScreenSimplified) viewModel.setShowScreenSimplified()
                }

                viewModel.set(parentFragmentManager)
                viewModel.initUploadsRecyclerView(binding.rvUploads, this)
                viewModel.initSwipeRefreshLayout(binding.swipeRefreshLayout)
                viewModel.loadRootFiles()
                initDownloadsRecyclerView(binding.rvDownloads)
                initFilesRecyclerView(binding.rvFiles, showScreenSimplified)
                viewModel.registerDeviceForFCM()
            }
        }
        return binding.root
    }

    private fun navigateToSharePreviewFragment(shareLinkUrlToken: String) {
        val bundle = bundleOf(URL_TOKEN_KEY to shareLinkUrlToken)
        findNavController().navigate(R.id.action_myFilesFragment_to_sharePreviewFragment, bundle)
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
            viewModel.upload(fileUriList)
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

    private val onNewTemporaryFile = Observer<Record> {
        recordsAdapter.addRecord(it)
    }

    private val onShowRecordSearchFragment = Observer<Void> {
        findNavController().navigate(R.id.action_myFilesFragment_to_recordSearchFragment)
    }

    private val onShowAddOptionsFragment = Observer<NavigationFolderIdentifier> {
        addOptionsFragment = AddOptionsFragment()
        addOptionsFragment?.setBundleArguments(it, false)
        addOptionsFragment?.show(parentFragmentManager, addOptionsFragment?.tag)
        addOptionsFragment?.getOnRefreshFolder()?.observe(this, onRefreshFolder)
    }

    private val onShowRecordOptionsFragment = Observer<Record> {
        recordOptionsFragment = RecordOptionsFragment()
        recordOptionsFragment?.setBundleArguments(
            it,
            isShownInMyFilesFragment = true,
            isShownInPublicFilesFragment = false,
            isShownInSharesFragment = false
        )
        recordOptionsFragment?.show(parentFragmentManager, recordOptionsFragment?.tag)
        recordOptionsFragment?.getOnFileDownloadRequest()?.observe(this, onFileDownloadRequest)
        recordOptionsFragment?.getOnRecordDeleteRequest()?.observe(this, onRecordDeleteRequest)
        recordOptionsFragment?.getOnRecordRenameRequest()?.observe(this, onRecordRenameRequest)
        recordOptionsFragment?.getOnRecordShareViaPermanentRequest()
            ?.observe(this, onRecordShareViaPermanentRequest)
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

    private val onFileViewRequest = Observer<ArrayList<Record>> {
        val bundle = bundleOf(PARCELABLE_FILES_KEY to it)
        findNavController().navigate(R.id.action_myFilesFragment_to_fileActivity, bundle)
    }

    private val onPhotoSelectedObserver = Observer<Record> {
        onPhotoSelectedEvent.value = it
    }

    private val onRecordShareViaPermanentRequest = Observer<Record> { record ->
        navigateToShareLinkFragment(record)
    }

    private fun navigateToShareLinkFragment(record: Record?) {
        val bundle = bundleOf(PARCELABLE_RECORD_KEY to record)
        findNavController().navigate(R.id.action_myFilesFragment_to_shareLinkFragment, bundle)
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

    private fun initFilesRecyclerView(rvFiles: RecyclerView, showScreenSimplified: Boolean) {
        recordsRecyclerView = rvFiles
        recordsListAdapter = RecordsListAdapter(
            this,
            showScreenSimplified,
            viewModel.getIsRelocationMode(),
            isForSharesScreen = false,
            isForSearchScreen = false,
            recordListener = viewModel
        )
        recordsGridAdapter = RecordsGridAdapter(
            this,
            showScreenSimplified,
            viewModel.getIsRelocationMode(),
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
            addItemDecoration(
                DividerItemDecoration(
                    this.context,
                    DividerItemDecoration.VERTICAL
                )
            )
            setHasFixedSize(true)
        }
    }

    fun getOnPhotoSelected(): MutableLiveData<Record> = onPhotoSelectedEvent

    override fun connectViewModelEvents() {
        viewModel.getOnShowMessage().observe(this, onShowMessage)
        viewModel.getOnShowQuotaExceeded().observe(this, onShowQuotaExceeded)
        viewModel.getOnChangeViewMode().observe(this, onChangeViewMode)
        viewModel.getOnDownloadsRetrieved().observe(this, onDownloadsRetrieved)
        viewModel.getOnDownloadFinished().observe(this, onDownloadFinished)
        viewModel.getOnRecordsRetrieved().observe(this, onRecordsRetrieved)
        viewModel.getOnNewTemporaryFile().observe(this, onNewTemporaryFile)
        viewModel.getOnShowAddOptionsFragment().observe(this, onShowAddOptionsFragment)
        viewModel.getOnShowRecordOptionsFragment().observe(this, onShowRecordOptionsFragment)
        viewModel.getOnShowRecordSearchFragment().observe(this, onShowRecordSearchFragment)
        viewModel.getOnShowSortOptionsFragment().observe(this, onShowSortOptionsFragment)
        viewModel.getOnRecordDeleteRequest().observe(this, onRecordDeleteRequest)
        viewModel.getOnCancelAllUploads().observe(this, onCancelAllUploads)
        viewModel.getOnFileViewRequest().observe(this, onFileViewRequest)
        viewModel.getOnPhotoSelected().observe(this, onPhotoSelectedObserver)
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
        viewModel.getOnNewTemporaryFile().removeObserver(onNewTemporaryFile)
        viewModel.getOnShowAddOptionsFragment().removeObserver(onShowAddOptionsFragment)
        viewModel.getOnShowRecordOptionsFragment().removeObserver(onShowRecordOptionsFragment)
        viewModel.getOnShowRecordSearchFragment().removeObserver(onShowRecordSearchFragment)
        viewModel.getOnShowSortOptionsFragment().removeObserver(onShowSortOptionsFragment)
        viewModel.getOnRecordDeleteRequest().removeObserver(onRecordDeleteRequest)
        viewModel.getOnCancelAllUploads().removeObserver(onCancelAllUploads)
        viewModel.getOnFileViewRequest().removeObserver(onFileViewRequest)
        viewModel.getOnPhotoSelected().removeObserver(onPhotoSelectedObserver)
        renameDialogViewModel.getOnRecordRenamed().removeObserver(onRecordRenamed)
        renameDialogViewModel.getOnShowMessage().removeObserver(onShowMessage)
        addOptionsFragment?.getOnFilesSelected()?.removeObserver(onFilesSelectedToUpload)
        addOptionsFragment?.getOnRefreshFolder()?.removeObserver(onRefreshFolder)
        recordOptionsFragment?.getOnFileDownloadRequest()?.removeObserver(onFileDownloadRequest)
        recordOptionsFragment?.getOnRecordDeleteRequest()?.removeObserver(onRecordDeleteRequest)
        recordOptionsFragment?.getOnRecordRenameRequest()?.removeObserver(onRecordRenameRequest)
        recordOptionsFragment?.getOnRecordShareViaPermanentRequest()
            ?.removeObserver(onRecordShareViaPermanentRequest)
        recordOptionsFragment?.getOnRecordRelocateRequest()?.removeObserver(onRecordRelocateRequest)
        sortOptionsFragment?.getOnSortRequest()?.removeObserver(onSortRequest)
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