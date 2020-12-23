package org.permanent.permanent.ui.members

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import org.permanent.permanent.R
import org.permanent.permanent.databinding.DialogAddMemberBinding
import org.permanent.permanent.databinding.FragmentMembersBinding
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Account
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.AddMemberViewModel
import org.permanent.permanent.viewmodels.MembersViewModel


const val SNACKBAR_DURATION_MILLIS = 4000

class MembersFragment : PermanentBaseFragment() {

    private lateinit var viewModel: MembersViewModel
    private lateinit var binding: FragmentMembersBinding
    private lateinit var dialogViewModel: AddMemberViewModel
    private lateinit var dialogBinding: DialogAddMemberBinding
    private var alertDialog: AlertDialog? = null
    private val accessRoleList = listOf(
        AccessRole.MANAGER.toTitleCase(),
        AccessRole.CURATOR.toTitleCase(),
        AccessRole.EDITOR.toTitleCase(),
        AccessRole.CONTRIBUTOR.toTitleCase(),
        AccessRole.VIEWER.toTitleCase()
    )
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
        dialogViewModel = ViewModelProvider(this).get(AddMemberViewModel::class.java)

        initManagersRecyclerView(binding.rvManagers)
        initCuratorsRecyclerView(binding.rvCurators)
        initEditorsRecyclerView(binding.rvEditors)
        initContributorsRecyclerView(binding.rvContributors)
        initViewersRecyclerView(binding.rvViewers)

        return binding.root
    }

    private fun initManagersRecyclerView(rvManagers: RecyclerView) {
        managersRecyclerView = rvManagers
        managersAdapter = MembersAdapter()
        managersRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = managersAdapter
        }
    }

    private fun initCuratorsRecyclerView(rvCurators: RecyclerView) {
        curatorsRecyclerView = rvCurators
        curatorsAdapter = MembersAdapter()
        curatorsRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = curatorsAdapter
        }
    }

    private fun initEditorsRecyclerView(rvEditors: RecyclerView) {
        editorsRecyclerView = rvEditors
        editorsAdapter = MembersAdapter()
        editorsRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = editorsAdapter
        }
    }

    private fun initContributorsRecyclerView(rvContributors: RecyclerView) {
        contributorsRecyclerView = rvContributors
        contributorsAdapter = MembersAdapter()
        contributorsRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = contributorsAdapter
        }
    }

    private fun initViewersRecyclerView(rvViewers: RecyclerView) {
        viewersRecyclerView = rvViewers
        viewersAdapter = MembersAdapter()
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

    private val onShowMessage = Observer<String> { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    @SuppressLint("WrongConstant")
    private val onShowSnackbar = Observer<Int> {
        Snackbar.make(binding.root, it, SNACKBAR_DURATION_MILLIS).show()
    }

    private val onShowAddDialogRequest = Observer<Void> {
        val accessLevelAdapter = ArrayAdapter(
            requireContext(),
            R.layout.menu_item_dropdown_add_member,
            accessRoleList
        )
        dialogBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_add_member, null, false
        )
        dialogBinding.executePendingBindings()
        dialogBinding.lifecycleOwner = this
        dialogBinding.viewModel = dialogViewModel
        dialogBinding.actvAccessLevel.setAdapter(accessLevelAdapter)
        val thisContext = context

        if (thisContext != null) {
            alertDialog = AlertDialog.Builder(thisContext)
                .setView(dialogBinding.root)
                .create()
            dialogBinding.btnSave.setOnClickListener {
//                val currentFolderIdentifier =
//                    arguments?.getParcelable<FolderIdentifier>(FOLDER_IDENTIFIER_KEY)
//                dialogViewModel.createNewFolder(currentFolderIdentifier)
            }
            dialogBinding.btnCancel.setOnClickListener {
                alertDialog?.dismiss()
            }
            alertDialog?.show()
        }
    }

    override fun connectViewModelEvents() {
        viewModel.getOnManagersRetrieved().observe(this, onManagersRetrieved)
        viewModel.getOnCuratorsRetrieved().observe(this, onCuratorsRetrieved)
        viewModel.getOnEditorsRetrieved().observe(this, onEditorsRetrieved)
        viewModel.getOnContributorsRetrieved().observe(this, onContributorsRetrieved)
        viewModel.getOnViewersRetrieved().observe(this, onViewersRetrieved)
        viewModel.getShowMessage().observe(this, onShowMessage)
        viewModel.getShowSnackbar().observe(this, onShowSnackbar)
        viewModel.getShowAddDialogRequest().observe(this, onShowAddDialogRequest)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnManagersRetrieved().removeObserver(onManagersRetrieved)
        viewModel.getOnCuratorsRetrieved().removeObserver(onCuratorsRetrieved)
        viewModel.getOnEditorsRetrieved().removeObserver(onEditorsRetrieved)
        viewModel.getOnContributorsRetrieved().removeObserver(onContributorsRetrieved)
        viewModel.getOnViewersRetrieved().removeObserver(onViewersRetrieved)
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getShowSnackbar().removeObserver(onShowSnackbar)
        viewModel.getShowAddDialogRequest().removeObserver(onShowAddDialogRequest)
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