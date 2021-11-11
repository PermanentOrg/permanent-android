package org.permanent.permanent.ui.myFiles.linkshare

import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.dialog_delete.view.*
import org.permanent.permanent.R
import org.permanent.permanent.databinding.DialogEditAccessLevelBinding
import org.permanent.permanent.databinding.FragmentShareLinkBinding
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.Share
import org.permanent.permanent.models.ShareByUrl
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.fileView.FileActivity
import org.permanent.permanent.ui.hideKeyboardFrom
import org.permanent.permanent.ui.members.ItemOptionsFragment
import org.permanent.permanent.ui.myFiles.PARCELABLE_RECORD_KEY
import org.permanent.permanent.viewmodels.EditAccessLevelViewModel
import org.permanent.permanent.viewmodels.ShareLinkViewModel
import java.util.*

const val PARCELABLE_SHARE_KEY = "parcelable_share_key"

class ShareLinkFragment : PermanentBaseFragment() {

    private lateinit var viewModel: ShareLinkViewModel
    private lateinit var binding: FragmentShareLinkBinding
    private lateinit var sharesRecyclerView: RecyclerView
    private lateinit var sharesAdapter: SharesAdapter
    private var record: Record? = null
    private var itemOptionsFragment: ItemOptionsFragment? = null
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(ShareLinkViewModel::class.java)
        binding = FragmentShareLinkBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        record = arguments?.getParcelable(PARCELABLE_RECORD_KEY)
        record?.let {
            viewModel.setRecord(it)
            initSharesRecyclerView(binding.rvShares, it)
        }
        editDialogViewModel = ViewModelProvider(this).get(EditAccessLevelViewModel::class.java)
        accessLevelAdapter = ArrayAdapter(
            requireContext(),
            R.layout.menu_item_dropdown_access_level,
            accessRoleList
        )
        if(activity is FileActivity){
            (activity as FileActivity?)!!.window?.statusBarColor = ResourcesCompat.getColor(resources, R.color.colorPrimary, null)
            (activity as FileActivity?)!!.setToolbarColor(R.color.colorPrimary)
        }
        return binding.root
    }

    private fun initSharesRecyclerView(rvShares: RecyclerView, record: Record) {
        sharesRecyclerView = rvShares
        sharesAdapter = SharesAdapter(this, record.shares, viewModel)
        sharesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = sharesAdapter
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Device's back press
        requireActivity().onBackPressedDispatcher
            .addCallback(this , object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed(){
                    if(activity is FileActivity)
                    {
                        (activity as FileActivity?)!!.window?.statusBarColor = ResourcesCompat.getColor(resources, R.color.black, null)
                        (activity as FileActivity?)!!.setToolbarColor(R.color.black)
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

    private val onLinkSettingsRequest = Observer<ShareByUrl> {
        val bundle = bundleOf(PARCELABLE_RECORD_KEY to record, PARCELABLE_SHARE_KEY to it)
        findNavController().navigate(R.id.action_shareLinkFragment_to_linkSettingsFragment, bundle)
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
        viewDialog.tvTitle.text = getString(R.string.share_link_revoke_title)
        viewDialog.btnDelete.text = getString(R.string.share_link_revoke_button)
        viewDialog.btnDelete.setOnClickListener {
            viewModel.deleteShareLink()
            alert.dismiss()
        }
        viewDialog.btnCancel.setOnClickListener {
            alert.dismiss()
        }
        alert.show()
    }

    private val onShareRemoved = Observer<Share> {
        sharesAdapter.remove(it)
        record?.shares?.remove(it)
        viewModel.getExistsShares().value = !record?.shares.isNullOrEmpty()
    }

    private val onItemUpdated = Observer<Void> {
        alertDialog?.dismiss()
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
        viewModel.getOnLinkSettingsRequest().observe(this, onLinkSettingsRequest)
        viewModel.getOnShowShareOptionsRequest().observe(this, onShowShareOptionsObserver)
        viewModel.getOnRevokeLinkRequest().observe(this, onRevokeLinkRequest)
        viewModel.getOnShareDenied().observe(this, onShareRemoved)
        editDialogViewModel.getOnItemEdited().observe(this, onItemUpdated)
        editDialogViewModel.getShowSuccessSnackbar().observe(this, showSnackbarSuccess)
        editDialogViewModel.getShowSnackbar().observe(this, showSnackbar)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowSnackbar().removeObserver(showSnackbar)
        viewModel.getShowSnackbarSuccess().removeObserver(showSnackbarSuccess)
        viewModel.getOnLinkSettingsRequest().removeObserver(onLinkSettingsRequest)
        viewModel.getOnShowShareOptionsRequest().removeObserver(onShowShareOptionsObserver)
        viewModel.getOnRevokeLinkRequest().removeObserver(onRevokeLinkRequest)
        viewModel.getOnShareDenied().removeObserver(onShareRemoved)
        itemOptionsFragment?.getShowEditShareDialogRequest()?.removeObserver(onShowEditShareDialog)
        itemOptionsFragment?.getOnShareRemoved()?.removeObserver(onShareRemoved)
        itemOptionsFragment?.getShowSnackbar()?.removeObserver(showSnackbar)
        itemOptionsFragment?.getShowSnackbarSuccess()?.removeObserver(showSnackbarSuccess)
        editDialogViewModel.getOnItemEdited().removeObserver(onItemUpdated)
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