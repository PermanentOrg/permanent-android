package org.permanent.permanent.ui.shareManagement

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.dialog_delete.view.*
import org.permanent.permanent.R
import org.permanent.permanent.databinding.DialogEditAccessLevelBinding
import org.permanent.permanent.databinding.FragmentShareManagementBinding
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.Share
import org.permanent.permanent.network.models.Shareby_urlVO
import org.permanent.permanent.ui.AccessRolesFragment
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.ui.fileView.FileActivity
import org.permanent.permanent.ui.hideKeyboardFrom
import org.permanent.permanent.ui.members.ItemOptionsFragment
import org.permanent.permanent.ui.myFiles.PARCELABLE_RECORD_KEY
import org.permanent.permanent.viewmodels.EditAccessLevelViewModel
import org.permanent.permanent.viewmodels.ShareManagementViewModel
import java.util.*

class ShareManagementFragment : PermanentBottomSheetFragment() {

    private lateinit var viewModel: ShareManagementViewModel
    private lateinit var binding: FragmentShareManagementBinding
    private lateinit var pendingSharesRecyclerView: RecyclerView
    private lateinit var pendingSharesAdapter: SharesAdapter
    private lateinit var sharesRecyclerView: RecyclerView
    private lateinit var sharesAdapter: SharesAdapter
    private var record: Record? = null
    private var itemOptionsFragment: ItemOptionsFragment? = null
    private var accessRolesFragment: AccessRolesFragment? = null
    private lateinit var editDialogViewModel: EditAccessLevelViewModel
    private lateinit var editDialogBinding: DialogEditAccessLevelBinding
    private lateinit var accessLevelAdapter: ArrayAdapter<String>
    private var alertDialog: androidx.appcompat.app.AlertDialog? = null
    private val accessRoleList = listOf(
        AccessRole.OWNER.toTitleCase(),
        AccessRole.CURATOR.toTitleCase(),
        AccessRole.EDITOR.toTitleCase(),
        AccessRole.CONTRIBUTOR.toTitleCase(),
        AccessRole.VIEWER.toTitleCase()
    )

    fun setBundleArguments(record: Record?, shareByUrlVO: Shareby_urlVO?) {
        val bundle = bundleOf(PARCELABLE_RECORD_KEY to record, SHARE_BY_URL_VO_KEY to shareByUrlVO)
        this.arguments = bundle
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[ShareManagementViewModel::class.java]
        binding = FragmentShareManagementBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        record = arguments?.getParcelable(PARCELABLE_RECORD_KEY)
        record?.let {
            viewModel.setRecord(it)
            initPendingSharesRecyclerView(binding.rvPendingShares)
            initSharesRecyclerView(binding.rvShares)
        }
        viewModel.setShareLink(arguments?.getParcelable(SHARE_BY_URL_VO_KEY))
        editDialogViewModel = ViewModelProvider(this)[EditAccessLevelViewModel::class.java]
        accessLevelAdapter = ArrayAdapter(
            requireContext(),
            R.layout.menu_item_dropdown_access_level,
            accessRoleList
        )
        if (activity is FileActivity) {
            (activity as FileActivity).setToolbarAndStatusBarColor(R.color.colorPrimary)
        }
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener { dialog: DialogInterface ->
            val dialogc = dialog as BottomSheetDialog
            val bottomSheet =
                dialogc.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from(bottomSheet as FrameLayout)
                .setState(BottomSheetBehavior.STATE_EXPANDED)
        }
        return bottomSheetDialog
    }

    private fun initPendingSharesRecyclerView(rvPendingShares: RecyclerView) {
        pendingSharesRecyclerView = rvPendingShares
        pendingSharesAdapter = SharesAdapter(viewModel.getPendingShares(), viewModel)
        pendingSharesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = pendingSharesAdapter
        }
    }

    private fun initSharesRecyclerView(rvShares: RecyclerView) {
        sharesRecyclerView = rvShares
        sharesAdapter = SharesAdapter(viewModel.getShares(), viewModel)
        sharesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = sharesAdapter
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Device's back press
        requireActivity().onBackPressedDispatcher
            .addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (activity is FileActivity) {
                        (activity as FileActivity).setToolbarAndStatusBarColor(R.color.black)
                    }
                    findNavController().popBackStack(R.id.shareLinkFragment, true)
                }
            })
    }

    private val showSnackbarSuccess = Observer<String> { message ->
        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        val view: View = snackBar.view
        context?.let {
            view.setBackgroundColor(ContextCompat.getColor(it, R.color.paleGreen))
            snackBar.setTextColor(ContextCompat.getColor(it, R.color.green))
        }
        val snackbarTextTextView = view.findViewById(R.id.snackbar_text) as TextView
        snackbarTextTextView.setTypeface(snackbarTextTextView.typeface, Typeface.BOLD)
        snackBar.show()
    }

    private val showSnackbar = Observer<String> { message ->
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private val onShareLinkObserver = Observer<String> {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, it)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private val onShowShareOptionsObserver = Observer<Share> { share ->
        itemOptionsFragment = ItemOptionsFragment()
        itemOptionsFragment?.setBundleArguments(share)
        itemOptionsFragment?.show(parentFragmentManager, itemOptionsFragment?.tag)
        itemOptionsFragment?.getShowEditShareDialogRequest()?.observe(this, onShowEditShareDialog)
        itemOptionsFragment?.getOnShareRemoved()?.observe(this, onShareRemoved)
        itemOptionsFragment?.getShowSnackbar()?.observe(this, showSnackbar)
        itemOptionsFragment?.getShowSnackbarSuccess()?.observe(this, showSnackbarSuccess)
    }

    private val onRevokeLinkRequest = Observer<Void> {
        val viewDialog: View = layoutInflater.inflate(R.layout.dialog_delete, null)
        val alert = AlertDialog.Builder(context)
            .setView(viewDialog)
            .create()
        viewDialog.tvTitle.text = getString(R.string.share_management_revoke_title)
        viewDialog.btnDelete.text = getString(R.string.share_management_revoke_button)
        viewDialog.btnDelete.setOnClickListener {
            viewModel.deleteShareLink()
            alert.dismiss()
        }
        viewDialog.btnCancel.setOnClickListener {
            alert.dismiss()
        }
        alert.show()
    }

    private val onShareApproved = Observer<Share> {
        pendingSharesAdapter.remove(it)
        sharesAdapter.add(it)
    }

    private val onShareRemoved = Observer<Share> {
        viewModel.onShareRemoved(it)
        sharesAdapter.remove(it)
        record?.shares?.remove(it)
    }

    private val onItemUpdated = Observer<Void> {
        alertDialog?.dismiss()
    }

    private val onAccessRoleUpdatedObserver = Observer<AccessRole> {
        viewModel.onAccessRoleUpdated(it)
    }

    private val onShowAccessRoles = Observer<Shareby_urlVO> {
        accessRolesFragment = AccessRolesFragment()
        accessRolesFragment?.setBundleArguments(it)
        accessRolesFragment?.getOnAccessRoleUpdated()?.observe(this, onAccessRoleUpdatedObserver)
        accessRolesFragment?.show(parentFragmentManager, accessRolesFragment?.tag)
    }

    private val onShowDatePicker = Observer<Void> {
        context?.let { context ->
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(context, viewModel, year, month, day).show()
        }
    }

    private val onShowEditShareDialog = Observer<Share> {
        editDialogViewModel.setShare(it)
        editDialogBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_edit_access_level, null, false
        )
        editDialogBinding.executePendingBindings()
        editDialogBinding.lifecycleOwner = this
        editDialogBinding.viewModel = editDialogViewModel
        editDialogBinding.actvAccessLevel.setText(it.accessRole?.toTitleCase())
        // setAdapter after setText in order to work properly
        editDialogBinding.actvAccessLevel.setAdapter(accessLevelAdapter)
        editDialogBinding.actvAccessLevel.setOnClickListener {
            context?.hideKeyboardFrom(editDialogBinding.root.windowToken)
        }
        editDialogBinding.actvAccessLevel.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val selectedRole = accessLevelAdapter.getItem(position) as String
                editDialogViewModel.setAccessLevel(
                    AccessRole.valueOf(selectedRole.uppercase(Locale.getDefault()))
                )
            }
        context?.let { ctx ->
            alertDialog = androidx.appcompat.app.AlertDialog.Builder(ctx)
                .setView(editDialogBinding.root)
                .create()
            editDialogBinding.btnCancel.setOnClickListener {
                alertDialog?.dismiss()
            }
            alertDialog?.show()
        }
    }

    override fun connectViewModelEvents() {
        viewModel.getShowSnackbar().observe(this, showSnackbar)
        viewModel.getShowSnackbarSuccess().observe(this, showSnackbarSuccess)
        viewModel.getOnShareLinkRequest().observe(this, onShareLinkObserver)
        viewModel.getOnShowShareOptionsRequest().observe(this, onShowShareOptionsObserver)
        viewModel.getOnRevokeLinkRequest().observe(this, onRevokeLinkRequest)
        viewModel.getOnShareApproved().observe(this, onShareApproved)
        viewModel.getOnShareDenied().observe(this, onShareRemoved)
        viewModel.getShowAccessRoles().observe(this, onShowAccessRoles)
        viewModel.getShowDatePicker().observe(this, onShowDatePicker)
        editDialogViewModel.getOnItemEdited().observe(this, onItemUpdated)
        editDialogViewModel.getShowSuccessSnackbar().observe(this, showSnackbarSuccess)
        editDialogViewModel.getShowSnackbar().observe(this, showSnackbar)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowSnackbar().removeObserver(showSnackbar)
        viewModel.getShowSnackbarSuccess().removeObserver(showSnackbarSuccess)
        viewModel.getOnShareLinkRequest().removeObserver(onShareLinkObserver)
        viewModel.getOnShowShareOptionsRequest().removeObserver(onShowShareOptionsObserver)
        viewModel.getOnRevokeLinkRequest().removeObserver(onRevokeLinkRequest)
        viewModel.getOnShareApproved().removeObserver(onShareApproved)
        viewModel.getOnShareDenied().removeObserver(onShareRemoved)
        viewModel.getShowAccessRoles().removeObserver(onShowAccessRoles)
        viewModel.getShowDatePicker().removeObserver(onShowDatePicker)
        itemOptionsFragment?.getShowEditShareDialogRequest()?.removeObserver(onShowEditShareDialog)
        itemOptionsFragment?.getOnShareRemoved()?.removeObserver(onShareRemoved)
        itemOptionsFragment?.getShowSnackbar()?.removeObserver(showSnackbar)
        itemOptionsFragment?.getShowSnackbarSuccess()?.removeObserver(showSnackbarSuccess)
        editDialogViewModel.getOnItemEdited().removeObserver(onItemUpdated)
        accessRolesFragment?.getOnAccessRoleUpdated()?.removeObserver(onAccessRoleUpdatedObserver)
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }

    companion object {
        const val PARCELABLE_SHARE_KEY = "parcelable_share_key"
        const val SHARE_BY_URL_VO_KEY = "share_by_url_vo_key"
    }
}