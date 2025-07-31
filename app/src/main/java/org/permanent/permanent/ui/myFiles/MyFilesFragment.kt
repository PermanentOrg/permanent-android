package org.permanent.permanent.ui.myFiles

import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
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
import org.permanent.permanent.databinding.FragmentMyFilesBinding
import org.permanent.permanent.models.AccountEventAction
import org.permanent.permanent.models.Download
import org.permanent.permanent.models.FileSessionData
import org.permanent.permanent.models.NavigationFolderIdentifier
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.models.ChecklistItem
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.SelectionOptionsFragment
import org.permanent.permanent.ui.Workspace
import org.permanent.permanent.ui.activities.MainActivity
import org.permanent.permanent.ui.archives.PARCELABLE_ARCHIVE_KEY
import org.permanent.permanent.ui.hideKeyboardFrom
import org.permanent.permanent.ui.myFiles.checklist.ChecklistBottomSheetFragment
import org.permanent.permanent.ui.myFiles.checklist.ChecklistItemType
import org.permanent.permanent.ui.myFiles.checklist.toChecklistType
import org.permanent.permanent.ui.myFiles.download.DownloadsAdapter
import org.permanent.permanent.ui.myFiles.saveToPermanent.SaveToPermanentFragment
import org.permanent.permanent.ui.openLink
import org.permanent.permanent.ui.public.PublicFragment
import org.permanent.permanent.ui.shareManagement.ShareManagementFragment
import org.permanent.permanent.ui.shares.PreviewState
import org.permanent.permanent.ui.shares.SHOW_SCREEN_SIMPLIFIED_KEY
import org.permanent.permanent.ui.shares.URL_TOKEN_KEY
import org.permanent.permanent.ui.storage.RedeemCodeFragment
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
    private var saveToPermanentFragment: SaveToPermanentFragment? = null
    private var shareManagementFragment: ShareManagementFragment? = null
    private var sortOptionsFragment: SortOptionsFragment? = null
    private var selectionOptionsFragment: SelectionOptionsFragment? = null
    private var bottomSheetFragment: ChecklistBottomSheetFragment? = null
    private val onRecordSelectedEvent = SingleLiveEvent<Record>()
    private var shouldRefreshCurrentFolder = false
    private var showScreenSimplified = false
    private var islandExpandedWidth = 960

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyFilesBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[MyFilesViewModel::class.java]
        renameDialogViewModel = ViewModelProvider(this)[RenameRecordViewModel::class.java]

        viewModel.sendEvent(AccountEventAction.OPEN_PRIVATE_WORKSPACE, data = mapOf("workspace" to "Private Files"))
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        islandExpandedWidth = displayMetrics.widthPixels - 50

        val record: Record? = arguments?.getParcelable(PARCELABLE_RECORD_KEY)
        if (record != null) {
            // notification deeplink
            showShareManagementFragment(record)
            arguments?.clear()
        } else {
            prefsHelper = PreferencesHelper(
                requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            )
            val shareLinkUrlToken = prefsHelper.getShareLinkUrlToken()
            val deepLinkArchiveNr = prefsHelper.getDeepLinkArchiveNr()
            val showArchivesScreen = prefsHelper.showArchivesScreen()

            if (!shareLinkUrlToken.isNullOrEmpty()) {
                // click on shareLinkUrl wasn't consumed
                navigateToSharePreviewFragment(shareLinkUrlToken)
            } else if (!deepLinkArchiveNr.isNullOrEmpty()) {
                // click on public profile link wasn't consumed
                navigateToPublicFragment(deepLinkArchiveNr)
            } else if (showArchivesScreen) {
                // click on archive invitations link wasn't consumed
                navigateToArchivesFragment()
                // marking it consumed
                prefsHelper.saveShowArchivesDeepLink(false)
            } else if (prefsHelper.showRedeemCodeScreen()) {
                // click on redeem code link wasn't consumed
                val bundle = bundleOf(RedeemCodeFragment.DEEPLINK_PROMO_CODE_KEY to prefsHelper.getPromoCode())
                findNavController().navigate(R.id.action_myFilesFragment_to_redeemCodeFragment, bundle)
                // marking it consumed
                prefsHelper.saveShowRedeemCodeDeepLink(false)
                prefsHelper.savePromoCodeFromDeepLink("")
            } else {
                binding.executePendingBindings()
                binding.lifecycleOwner = this
                binding.viewModel = viewModel

                viewModel.set(parentFragmentManager)
                viewModel.initUploadsRecyclerView(binding.rvUploads, this)
                viewModel.initSwipeRefreshLayout(binding.swipeRefreshLayout)
                viewModel.loadRootFiles()
                initDownloadsRecyclerView(binding.rvDownloads)
                viewModel.getHideChecklist()
                viewModel.registerDeviceForFCM()

                arguments?.takeIf { it.containsKey(SHOW_SCREEN_SIMPLIFIED_KEY) }?.apply {
                    showScreenSimplified = getBoolean(SHOW_SCREEN_SIMPLIFIED_KEY)
                    if (showScreenSimplified) viewModel.setShowScreenSimplified()
                }
                initFilesRecyclerView(binding.rvFiles, showScreenSimplified)

                arguments?.getParcelableArrayList<Uri>(MainActivity.SAVE_TO_PERMANENT_FILE_URIS_KEY)
                    ?.let { showSaveToPermanentFragment(it) }
                if (viewModel.isRelocationMode.value == true) resizeIslandWidthAnimated(
                    binding.flFloatingActionIsland.width,
                    islandExpandedWidth
                )
            }
        }
        return binding.root
    }

    private fun navigateToSharePreviewFragment(shareLinkUrlToken: String) {
        val bundle = bundleOf(URL_TOKEN_KEY to shareLinkUrlToken)
        findNavController().navigate(R.id.action_myFilesFragment_to_sharePreviewFragment, bundle)
    }

    private fun navigateToPublicFragment(deepLinkArchiveNr: String) {
        val bundle = bundleOf(PublicFragment.ARCHIVE_NR to deepLinkArchiveNr)
        findNavController().navigate(R.id.action_myFilesFragment_to_publicFragment, bundle)
    }

    private fun navigateToArchivesFragment() {
        findNavController().navigate(R.id.action_myFilesFragment_to_archivesFragment)
    }

    private val onShowMessage = Observer<String> {
        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
    }

    private val onShowQuotaExceeded = Observer<Void?> {
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

    private val onFilesUploadRequest = Observer<Pair<Record?, List<Uri>>> {
        it.first?.let { folderRecord -> viewModel.onRecordClick(folderRecord) }
        viewModel.uploadToCurrentFolder(it.second)
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

    private val onShowRecordSearchFragment = Observer<Void?> {
        findNavController().navigate(R.id.action_myFilesFragment_to_recordSearchFragment)
    }

    private fun showSaveToPermanentFragment(uris: ArrayList<Uri>) {
        saveToPermanentFragment = SaveToPermanentFragment()
        saveToPermanentFragment?.setBundleArguments(uris)
        saveToPermanentFragment?.show(parentFragmentManager, saveToPermanentFragment?.tag)
    }

    private val onShowAddOptionsFragment = Observer<NavigationFolderIdentifier> {
        addOptionsFragment = AddOptionsFragment()
        addOptionsFragment?.setBundleArguments(it, false)
        addOptionsFragment?.show(parentFragmentManager, addOptionsFragment?.tag)
        addOptionsFragment?.getOnRefreshFolder()?.observe(this, onRefreshFolder)
    }

    private val onShowRecordOptionsFragment = Observer<Record> {
        recordOptionsFragment = RecordOptionsFragment()
        recordOptionsFragment?.setBundleArguments(it, Workspace.PRIVATE_FILES)
        recordOptionsFragment?.show(parentFragmentManager, recordOptionsFragment?.tag)
        recordOptionsFragment?.getOnFileDownloadRequest()?.observe(this, onFileDownloadRequest)
        recordOptionsFragment?.getOnRecordDeleteRequest()?.observe(this, onRecordDeleteRequest)
        recordOptionsFragment?.getOnRecordRenameRequest()?.observe(this, onRecordRenameRequest)
        recordOptionsFragment?.getOnRecordRelocateRequest()?.observe(this, onRecordRelocateObserver)
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

    private val onCancelAllUploads = Observer<Void?> {
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
        FileSessionData.records = it
        findNavController().navigate(R.id.action_myFilesFragment_to_fileActivity)
    }

    private val onRecordSelectedObserver = Observer<Record> {
        onRecordSelectedEvent.value = it
    }

    private fun showShareManagementFragment(record: Record?) {
        shareManagementFragment = ShareManagementFragment()
        shareManagementFragment?.setBundleArguments(record, null)
        shareManagementFragment?.show(parentFragmentManager, shareManagementFragment?.tag)
    }

    private val shrinkIslandRequestObserver = Observer<Void?> {
        resizeIslandWidthAnimated(binding.flFloatingActionIsland.width, ISLAND_WIDTH_SMALL)
    }

    private val expandIslandRequestObserver = Observer<Void?> {
        resizeIslandWidthAnimated(binding.flFloatingActionIsland.width, islandExpandedWidth)
    }

    private val deleteRecordsObserver = Observer<Void?> {
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

    private val refreshCurrentFolderObserver = Observer<Void?> {
        viewModel.refreshCurrentFolder()
    }

    private val showSelectionOptionsObserver = Observer<Pair<Int, Boolean>> {
        selectionOptionsFragment = SelectionOptionsFragment()
        selectionOptionsFragment?.setBundleArguments(it)
        selectionOptionsFragment?.show(parentFragmentManager, selectionOptionsFragment?.tag)
        selectionOptionsFragment?.getOnSelectionRelocateRequest()?.observe(this, onSelectionRelocateObserver)
    }

    private val showEditMetadataScreenObserver = Observer<MutableList<Record>> {
        val bundle = bundleOf(PARCELABLE_FILES_KEY to it)
        findNavController().navigate(R.id.action_myFilesFragment_to_editMetadataFragment, bundle)
    }

    private val openChecklistBottomSheetObserver = Observer<Void?> {
        bottomSheetFragment = ChecklistBottomSheetFragment()
        bottomSheetFragment?.show(parentFragmentManager, "ChecklistBottomSheet")
        bottomSheetFragment?.getOnChecklistItemClick()?.observe(this, onChecklistItemClickObserver)
        bottomSheetFragment?.getHideChecklistButton()?.observe(this, onHideChecklistButtonObserver)
    }

    private val onChecklistItemClickObserver = Observer<ChecklistItem> {
        when (it.toChecklistType()) {
            ChecklistItemType.STORAGE_REDEEMED -> findNavController().navigate(R.id.redeemCodeFragment)
            ChecklistItemType.LEGACY_CONTACT -> findNavController().navigate(R.id.legacyContactFragment)
            ChecklistItemType.ARCHIVE_STEWARD -> {
                val bundle = bundleOf(PARCELABLE_ARCHIVE_KEY to viewModel.getCurrentArchive())
                findNavController().navigate(R.id.archiveStewardFragment, bundle)
            }
            ChecklistItemType.FIRST_UPLOAD -> context?.openLink("https://permanent.zohodesk.com/portal/en/kb/articles/uploading-files-mobile-apps")
            ChecklistItemType.ARCHIVE_PROFILE -> {
                val bundle = bundleOf(PublicFragment.OPEN_PROFILE_TAB to true)
                findNavController().navigate(R.id.publicFragment, bundle)
            }
            ChecklistItemType.PUBLISH_CONTENT -> context?.openLink("https://permanent.zohodesk.com/portal/en/kb/articles/how-to-publish-a-file-or-folder-mobile")
            ChecklistItemType.ARCHIVE_CREATED, null -> {}
        }
    }

    private val onHideChecklistButtonObserver = Observer<Void?> {
        viewModel.hideChecklistButton()
    }

    private val onSelectionRelocateObserver = Observer<ModificationType> {
        viewModel.onSelectionRelocationBtnClick(it)
    }

    private val onRecordRelocateObserver = Observer<Pair<Record, ModificationType>> {
        viewModel.setRelocationMode(Pair(mutableListOf(it.first), it.second))
        lifecycleScope.launch {
            delay(DELAY_TO_RESIZE_MILLIS)
            resizeIslandWidthAnimated(binding.flFloatingActionIsland.width, islandExpandedWidth)
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

    private val onRefreshFolder = Observer<Void?> {
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

    private val onRecordRenamed = Observer<Void?> {
        viewModel.refreshCurrentFolder()
        alertDialog?.dismiss()
    }

    private val onCurrentArchiveChangedObserver = Observer<Void?> {
        viewModel.loadRootFiles()
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
            viewModel.getIsSelectionMode(),
            isForSharesScreen = false,
            isForSearchScreen = false,
            recordListener = viewModel
        )
        recordsGridAdapter = RecordsGridAdapter(
            this,
            showScreenSimplified,
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

    fun getOnRecordSelected(): MutableLiveData<Record> = onRecordSelectedEvent

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
        viewModel.getOnRecordSelected().observe(this, onRecordSelectedObserver)
        viewModel.getShrinkIslandRequest().observe(this, shrinkIslandRequestObserver)
        viewModel.getExpandIslandRequest().observe(this, expandIslandRequestObserver)
        viewModel.getDeleteRecordsRequest().observe(this, deleteRecordsObserver)
        viewModel.getRefreshCurrentFolderRequest().observe(this, refreshCurrentFolderObserver)
        viewModel.getShowSelectionOptionsRequest().observe(this, showSelectionOptionsObserver)
        viewModel.getShowEditMetadataScreenRequest().observe(this, showEditMetadataScreenObserver)
        viewModel.getOpenChecklistBottomSheet().observe(this, openChecklistBottomSheetObserver)
        renameDialogViewModel.getOnRecordRenamed().observe(this, onRecordRenamed)
        renameDialogViewModel.getOnShowMessage().observe(this, onShowMessage)
        addOptionsFragment?.getOnFilesSelected()?.observe(this, onFilesSelectedToUpload)
        saveToPermanentFragment?.getOnFilesUploadRequest()?.observe(this, onFilesUploadRequest)
        saveToPermanentFragment?.getOnCurrentArchiveChangedEvent()
            ?.observe(this, onCurrentArchiveChangedObserver)
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
        viewModel.getOnRecordSelected().removeObserver(onRecordSelectedObserver)
        viewModel.getShrinkIslandRequest().removeObserver(shrinkIslandRequestObserver)
        viewModel.getExpandIslandRequest().removeObserver(expandIslandRequestObserver)
        viewModel.getDeleteRecordsRequest().removeObserver(deleteRecordsObserver)
        viewModel.getRefreshCurrentFolderRequest().removeObserver(refreshCurrentFolderObserver)
        viewModel.getShowSelectionOptionsRequest().removeObserver(showSelectionOptionsObserver)
        viewModel.getShowEditMetadataScreenRequest().removeObserver(showEditMetadataScreenObserver)
        viewModel.getOpenChecklistBottomSheet().removeObserver(openChecklistBottomSheetObserver)
        bottomSheetFragment?.getOnChecklistItemClick()?.removeObserver(onChecklistItemClickObserver)
        bottomSheetFragment?.getHideChecklistButton()?.removeObserver(onHideChecklistButtonObserver)
        renameDialogViewModel.getOnRecordRenamed().removeObserver(onRecordRenamed)
        renameDialogViewModel.getOnShowMessage().removeObserver(onShowMessage)
        addOptionsFragment?.getOnFilesSelected()?.removeObserver(onFilesSelectedToUpload)
        addOptionsFragment?.getOnRefreshFolder()?.removeObserver(onRefreshFolder)
        saveToPermanentFragment?.getOnFilesUploadRequest()?.removeObserver(onFilesUploadRequest)
        saveToPermanentFragment?.getOnCurrentArchiveChangedEvent()
            ?.removeObserver(onCurrentArchiveChangedObserver)
        recordOptionsFragment?.getOnFileDownloadRequest()?.removeObserver(onFileDownloadRequest)
        recordOptionsFragment?.getOnRecordDeleteRequest()?.removeObserver(onRecordDeleteRequest)
        recordOptionsFragment?.getOnRecordRenameRequest()?.removeObserver(onRecordRenameRequest)
        recordOptionsFragment?.getOnRecordRelocateRequest()?.removeObserver(onRecordRelocateObserver)
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

    companion object {
        const val ISLAND_WIDTH_SMALL = 160
        const val RESIZE_DURATION_MILLIS = 500L
        const val DELAY_TO_RESIZE_MILLIS = 500L
    }
}