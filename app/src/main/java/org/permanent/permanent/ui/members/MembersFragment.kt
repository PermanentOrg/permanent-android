package org.permanent.permanent.ui.members

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import org.permanent.permanent.R
import org.permanent.permanent.databinding.DialogAddMemberBinding
import org.permanent.permanent.databinding.DialogEditAccessLevelBinding
import org.permanent.permanent.databinding.DialogTitleTextTwoButtonsBinding
import org.permanent.permanent.databinding.FragmentMembersBinding
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Account
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.hideKeyboardFrom
import org.permanent.permanent.viewmodels.AddMemberViewModel
import org.permanent.permanent.viewmodels.EditAccessLevelViewModel
import org.permanent.permanent.viewmodels.MembersViewModel
import java.util.Locale


const val SNACKBAR_DURATION_MILLIS = 5000

class MembersFragment : PermanentBaseFragment() {

    private lateinit var viewModel: MembersViewModel
    private lateinit var binding: FragmentMembersBinding
    private lateinit var addDialogViewModel: AddMemberViewModel
    private lateinit var addDialogBinding: DialogAddMemberBinding
    private lateinit var editDialogViewModel: EditAccessLevelViewModel
    private lateinit var editDialogBinding: DialogEditAccessLevelBinding
    private var alertDialog: AlertDialog? = null
    private val accessRoleList = listOf(
        AccessRole.OWNER.toTitleCase(),
        AccessRole.MANAGER.toTitleCase(),
        AccessRole.CURATOR.toTitleCase(),
        AccessRole.EDITOR.toTitleCase(),
        AccessRole.CONTRIBUTOR.toTitleCase(),
        AccessRole.VIEWER.toTitleCase()
    )
    private lateinit var accessLevelAdapter: ArrayAdapter<String>
    private lateinit var managersRecyclerView: RecyclerView
    private lateinit var curatorsRecyclerView: RecyclerView
    private lateinit var editorsRecyclerView: RecyclerView
    private lateinit var contributorsRecyclerView: RecyclerView
    private lateinit var viewersRecyclerView: RecyclerView
    private lateinit var managersAdapter: MembersAdapter
    private lateinit var curatorsAdapter: MembersAdapter
    private lateinit var editorsAdapter: MembersAdapter
    private lateinit var contributorsAdapter: MembersAdapter
    private lateinit var viewersAdapter: MembersAdapter
    private var itemOptionsFragment: ItemOptionsFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(MembersViewModel::class.java)
        binding = FragmentMembersBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        addDialogViewModel = ViewModelProvider(this).get(AddMemberViewModel::class.java)
        editDialogViewModel = ViewModelProvider(this).get(EditAccessLevelViewModel::class.java)
        accessLevelAdapter = ArrayAdapter(
            requireContext(),
            R.layout.menu_item_dropdown_access_level,
            accessRoleList
        )
        initManagersRecyclerView(binding.rvManagers)
        initCuratorsRecyclerView(binding.rvCurators)
        initEditorsRecyclerView(binding.rvEditors)
        initContributorsRecyclerView(binding.rvContributors)
        initViewersRecyclerView(binding.rvViewers)

        return binding.root
    }

    private fun initManagersRecyclerView(rvManagers: RecyclerView) {
        managersRecyclerView = rvManagers
        managersAdapter = MembersAdapter(viewModel)
        managersRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = managersAdapter
        }
    }

    private fun initCuratorsRecyclerView(rvCurators: RecyclerView) {
        curatorsRecyclerView = rvCurators
        curatorsAdapter = MembersAdapter(viewModel)
        curatorsRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = curatorsAdapter
        }
    }

    private fun initEditorsRecyclerView(rvEditors: RecyclerView) {
        editorsRecyclerView = rvEditors
        editorsAdapter = MembersAdapter(viewModel)
        editorsRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = editorsAdapter
        }
    }

    private fun initContributorsRecyclerView(rvContributors: RecyclerView) {
        contributorsRecyclerView = rvContributors
        contributorsAdapter = MembersAdapter(viewModel)
        contributorsRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = contributorsAdapter
        }
    }

    private fun initViewersRecyclerView(rvViewers: RecyclerView) {
        viewersRecyclerView = rvViewers
        viewersAdapter = MembersAdapter(viewModel)
        viewersRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = viewersAdapter
        }
    }

    private val onManagersRetrieved = Observer<List<Account>> {
        managersAdapter.set(it)
    }

    private val onCuratorsRetrieved = Observer<List<Account>> {
        curatorsAdapter.set(it)
    }

    private val onEditorsRetrieved = Observer<List<Account>> {
        editorsAdapter.set(it)
    }

    private val onContributorsRetrieved = Observer<List<Account>> {
        contributorsAdapter.set(it)
    }

    private val onViewersRetrieved = Observer<List<Account>> {
        viewersAdapter.set(it)
    }

    private val showSuccessSnackbarObserver = Observer<String> { message ->
        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        val view: View = snackBar.view
        context?.let {
            view.setBackgroundColor(ContextCompat.getColor(it, R.color.deepGreen))
            snackBar.setTextColor(ContextCompat.getColor(it, R.color.paleGreen))
        }
        val snackbarTextTextView: TextView = view.findViewById(R.id.snackbar_text)
        snackbarTextTextView.setTypeface(snackbarTextTextView.typeface, Typeface.BOLD)
        snackBar.show()
    }

    @SuppressLint("WrongConstant")
    private val onShowSnackbarLong = Observer<Int> {
        Snackbar.make(binding.root, it, SNACKBAR_DURATION_MILLIS).show()
    }

    private val showErrorSnackbarObserver = Observer<String> { message ->
        alertDialog?.dismiss()
        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        val view: View = snackBar.view
        context?.let { view.setBackgroundColor(ContextCompat.getColor(it, R.color.deepRed))
            snackBar.setTextColor(ContextCompat.getColor(it, R.color.white))
        }
        snackBar.show()
    }

    private val onShowAddMemberDialog = Observer<Void?> {
        addDialogBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_add_member, null, false
        )
        addDialogBinding.executePendingBindings()
        addDialogBinding.lifecycleOwner = this
        addDialogBinding.viewModel = addDialogViewModel
        addDialogViewModel.clearFields()
        addDialogBinding.actvAccessLevel.setOnClickListener {
            context?.hideKeyboardFrom(addDialogBinding.root.windowToken)
        }
        addDialogBinding.actvAccessLevel.setAdapter(accessLevelAdapter)
        addDialogBinding.actvAccessLevel.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val selectedRole = accessLevelAdapter.getItem(position) as String
                addDialogViewModel.setAccessRole(
                    AccessRole.valueOf(selectedRole.uppercase(Locale.getDefault()))
                )
            }
        context?.let { ctx ->
            alertDialog = AlertDialog.Builder(ctx)
                .setView(addDialogBinding.root)
                .create()
            addDialogBinding.btnCancel.setOnClickListener {
                alertDialog?.dismiss()
            }
            alertDialog?.show()
        }
    }

    private val onShowMemberOptionsFragment = Observer<Account> { member ->
        itemOptionsFragment = ItemOptionsFragment()
        itemOptionsFragment?.setBundleArguments(member)
        itemOptionsFragment?.show(parentFragmentManager, itemOptionsFragment?.tag)
        itemOptionsFragment?.getShowEditMemberDialogRequest()
            ?.observe(this, onShowEditMemberDialog)
        itemOptionsFragment?.getOnMemberRemoved()?.observe(this, onMemberRemoved)
    }

    private val onShowEditMemberDialog = Observer<Account> {
        editDialogViewModel.setMember(it)
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
            alertDialog = AlertDialog.Builder(ctx)
                .setView(editDialogBinding.root)
                .create()
            editDialogBinding.btnCancel.setOnClickListener {
                alertDialog?.dismiss()
            }
            alertDialog?.show()
        }
    }

    private val onMembersUpdated = Observer<Void?> {
        viewModel.refreshMembers()
        alertDialog?.dismiss()
    }

    private val onMemberRemoved = Observer<String> { message ->
        showSuccessSnackbarObserver.onChanged(message)
        viewModel.refreshMembers()
    }

    private val onOwnershipTransferRequest = Observer<Boolean> { isFromAddMemberDialog ->
        val dialogBinding: DialogTitleTextTwoButtonsBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.dialog_title_text_two_buttons, null, false
        )
        val alert = android.app.AlertDialog.Builder(context).setView(dialogBinding.root).create()

        dialogBinding.tvTitle.text = getString(R.string.dialog_transfer_ownership_title)
        dialogBinding.tvText.text = getString(R.string.dialog_transfer_ownership_text)
        dialogBinding.btnPositive.text = getString(R.string.button_transfer)
        dialogBinding.btnPositive.setOnClickListener {
            if (isFromAddMemberDialog) addDialogViewModel.transferOwnership()
            else editDialogViewModel.transferOwnership()
            alert.dismiss()
        }
        dialogBinding.btnNegative.setOnClickListener {
            alert.dismiss()
        }
        alert.show()
    }

    override fun connectViewModelEvents() {
        viewModel.getOnManagersRetrieved().observe(this, onManagersRetrieved)
        viewModel.getOnCuratorsRetrieved().observe(this, onCuratorsRetrieved)
        viewModel.getOnEditorsRetrieved().observe(this, onEditorsRetrieved)
        viewModel.getOnContributorsRetrieved().observe(this, onContributorsRetrieved)
        viewModel.getOnViewersRetrieved().observe(this, onViewersRetrieved)
        viewModel.getShowErrorSnackbar().observe(this, showErrorSnackbarObserver)
        viewModel.getShowSnackbarLong().observe(this, onShowSnackbarLong)
        viewModel.getShowAddMemberDialogRequest().observe(this, onShowAddMemberDialog)
        viewModel.getShowMemberOptionsFragmentRequest().observe(this, onShowMemberOptionsFragment)
        addDialogViewModel.getOnOwnershipTransferRequest()
            .observe(this, onOwnershipTransferRequest)
        addDialogViewModel.getOnMemberAddedConclusion().observe(this, onMembersUpdated)
        addDialogViewModel.getShowSuccessSnackbar().observe(this, showSuccessSnackbarObserver)
        addDialogViewModel.getShowErrorSnackbar().observe(this, showErrorSnackbarObserver)
        editDialogViewModel.getOnItemEdited().observe(this, onMembersUpdated)
        editDialogViewModel.getOnOwnershipTransferRequest()
            .observe(this, onOwnershipTransferRequest)
        editDialogViewModel.getShowSuccessSnackbar().observe(this, showSuccessSnackbarObserver)
        editDialogViewModel.getShowErrorSnackbar().observe(this, showErrorSnackbarObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnManagersRetrieved().removeObserver(onManagersRetrieved)
        viewModel.getOnCuratorsRetrieved().removeObserver(onCuratorsRetrieved)
        viewModel.getOnEditorsRetrieved().removeObserver(onEditorsRetrieved)
        viewModel.getOnContributorsRetrieved().removeObserver(onContributorsRetrieved)
        viewModel.getOnViewersRetrieved().removeObserver(onViewersRetrieved)
        viewModel.getShowErrorSnackbar().removeObserver(showErrorSnackbarObserver)
        viewModel.getShowSnackbarLong().removeObserver(onShowSnackbarLong)
        viewModel.getShowAddMemberDialogRequest().removeObserver(onShowAddMemberDialog)
        viewModel.getShowMemberOptionsFragmentRequest().removeObserver(onShowMemberOptionsFragment)
        addDialogViewModel.getOnOwnershipTransferRequest()
            .removeObserver(onOwnershipTransferRequest)
        addDialogViewModel.getOnMemberAddedConclusion().removeObserver(onMembersUpdated)
        addDialogViewModel.getShowSuccessSnackbar().removeObserver(showSuccessSnackbarObserver)
        addDialogViewModel.getShowErrorSnackbar().removeObserver(showErrorSnackbarObserver)
        editDialogViewModel.getOnItemEdited().removeObserver(onMembersUpdated)
        editDialogViewModel.getOnOwnershipTransferRequest()
            .removeObserver(onOwnershipTransferRequest)
        editDialogViewModel.getShowSuccessSnackbar().removeObserver(showSuccessSnackbarObserver)
        editDialogViewModel.getShowErrorSnackbar().removeObserver(showErrorSnackbarObserver)
        itemOptionsFragment?.getShowEditMemberDialogRequest()
            ?.removeObserver(onShowEditMemberDialog)
        itemOptionsFragment?.getOnMemberRemoved()?.removeObserver(onMemberRemoved)
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