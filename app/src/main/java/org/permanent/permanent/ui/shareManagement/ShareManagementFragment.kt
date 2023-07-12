package org.permanent.permanent.ui.shareManagement

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import org.permanent.permanent.R
import org.permanent.permanent.databinding.DialogDeleteBinding
import org.permanent.permanent.databinding.FragmentShareManagementBinding
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.Share
import org.permanent.permanent.network.models.Shareby_urlVO
import org.permanent.permanent.ui.AccessRolesFragment
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.ui.fileView.FileActivity
import org.permanent.permanent.ui.myFiles.PARCELABLE_RECORD_KEY
import org.permanent.permanent.viewmodels.ShareManagementViewModel
import java.util.Calendar

class ShareManagementFragment : PermanentBottomSheetFragment() {

    private lateinit var viewModel: ShareManagementViewModel
    private lateinit var binding: FragmentShareManagementBinding
    private lateinit var pendingSharesRecyclerView: RecyclerView
    private lateinit var pendingSharesAdapter: SharesAdapter
    private lateinit var sharesRecyclerView: RecyclerView
    private lateinit var sharesAdapter: SharesAdapter
    private var record: Record? = null
    private var shareToEdit: Share? = null
    private var accessRolesFragment: AccessRolesFragment? = null

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
            view.setBackgroundColor(ContextCompat.getColor(it, R.color.deepGreen))
            snackBar.setTextColor(ContextCompat.getColor(it, R.color.paleGreen))
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

    private val onRevokeLinkRequest = Observer<Void?> {
        val dialogBinding: DialogDeleteBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.dialog_delete, null, false
        )
        val alert = android.app.AlertDialog.Builder(context).setView(dialogBinding.root).create()

        dialogBinding.tvTitle.text = getString(R.string.share_management_revoke_title)
        dialogBinding.btnDelete.text = getString(R.string.share_management_revoke_button)
        dialogBinding.btnDelete.setOnClickListener {
            viewModel.deleteShareLink()
            alert.dismiss()
        }
        dialogBinding.btnCancel.setOnClickListener {
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

    private val showAccessRolesForShareObserver = Observer<Share> { share ->
        shareToEdit = share
        accessRolesFragment = AccessRolesFragment()
        accessRolesFragment?.setBundleArguments(share)
        accessRolesFragment?.getOnAccessRoleUpdated()
            ?.observe(this, onAccessRoleForShareUpdatedObserver)
        accessRolesFragment?.show(parentFragmentManager, accessRolesFragment?.tag)
    }

    private val showAccessRolesForLinkObserver = Observer<Shareby_urlVO> {
        accessRolesFragment = AccessRolesFragment()
        accessRolesFragment?.setBundleArguments(it)
        accessRolesFragment?.getOnAccessRoleUpdated()
            ?.observe(this, onAccessRoleForLinkUpdatedObserver)
        accessRolesFragment?.show(parentFragmentManager, accessRolesFragment?.tag)
    }

    private val onAccessRoleForLinkUpdatedObserver = Observer<AccessRole> {
        viewModel.onAccessRoleUpdated(it)
    }

    private val onAccessRoleForShareUpdatedObserver = Observer<AccessRole?> {
        if (it == null) {
            shareToEdit?.let { share -> onShareRemoved.onChanged(share) }
        } else {
            shareToEdit?.accessRole = it
            shareToEdit?.let { share -> sharesAdapter.update(share) }
        }
    }

    private val onShowDatePicker = Observer<Void?> {
        context?.let { context ->
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(context, viewModel, year, month, day).show()
        }
    }

    override fun connectViewModelEvents() {
        viewModel.getShowSnackbar().observe(this, showSnackbar)
        viewModel.getShowSnackbarSuccess().observe(this, showSnackbarSuccess)
        viewModel.getOnShareLinkRequest().observe(this, onShareLinkObserver)
        viewModel.getShowAccessRolesForShare().observe(this, showAccessRolesForShareObserver)
        viewModel.getOnRevokeLinkRequest().observe(this, onRevokeLinkRequest)
        viewModel.getOnShareApproved().observe(this, onShareApproved)
        viewModel.getOnShareDenied().observe(this, onShareRemoved)
        viewModel.getShowAccessRolesForLink().observe(this, showAccessRolesForLinkObserver)
        viewModel.getShowDatePicker().observe(this, onShowDatePicker)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowSnackbar().removeObserver(showSnackbar)
        viewModel.getShowSnackbarSuccess().removeObserver(showSnackbarSuccess)
        viewModel.getOnShareLinkRequest().removeObserver(onShareLinkObserver)
        viewModel.getShowAccessRolesForShare().removeObserver(showAccessRolesForShareObserver)
        viewModel.getOnRevokeLinkRequest().removeObserver(onRevokeLinkRequest)
        viewModel.getOnShareApproved().removeObserver(onShareApproved)
        viewModel.getOnShareDenied().removeObserver(onShareRemoved)
        viewModel.getShowAccessRolesForLink().removeObserver(showAccessRolesForLinkObserver)
        viewModel.getShowDatePicker().removeObserver(onShowDatePicker)
        accessRolesFragment?.getOnAccessRoleUpdated()
            ?.removeObserver(onAccessRoleForLinkUpdatedObserver)
        accessRolesFragment?.getOnAccessRoleUpdated()
            ?.removeObserver(onAccessRoleForShareUpdatedObserver)
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