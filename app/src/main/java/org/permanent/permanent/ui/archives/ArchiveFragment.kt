package org.permanent.permanent.ui.archives

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.dialog_delete.view.*
import org.permanent.permanent.R
import org.permanent.permanent.databinding.DialogCreateNewArchiveBinding
import org.permanent.permanent.databinding.FragmentArchiveBinding
import org.permanent.permanent.models.Archive
import org.permanent.permanent.models.ArchiveType
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.hideKeyboardFrom
import org.permanent.permanent.viewmodels.ArchiveViewModel
import org.permanent.permanent.viewmodels.CreateNewArchiveViewModel
import java.util.*

class ArchiveFragment : PermanentBaseFragment(), ArchiveListener, View.OnClickListener {

    private lateinit var binding: FragmentArchiveBinding
    private lateinit var viewModel: ArchiveViewModel
    private lateinit var pendingArchivesRecyclerView: RecyclerView
    private lateinit var pendingArchivesAdapter: PendingArchivesAdapter
    private lateinit var archivesRecyclerView: RecyclerView
    private lateinit var archivesAdapter: ArchivesAdapter
    private lateinit var dialogCreateArchiveViewModel: CreateNewArchiveViewModel
    private lateinit var dialogCreateArchiveBinding: DialogCreateNewArchiveBinding
    private var archiveOptionsFragment: ArchiveOptionsFragment? = null
    private var alertDialog: AlertDialog? = null
    private lateinit var archiveTypeAdapter: ArrayAdapter<String>
    private val archiveTypeList = listOf(
        ArchiveType.PERSON.toTitleCase(),
        ArchiveType.FAMILY.toTitleCase(),
        ArchiveType.ORGANIZATION.toTitleCase()
    )

    private val onShowMessage = Observer<String> { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private val onPendingArchivesRetrieved = Observer<List<Archive>> {
        pendingArchivesAdapter.set(it)
    }

    private val onArchivesRetrieved = Observer<List<Archive>> {
        val prefsHelper = PreferencesHelper(
            requireContext().getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
        )
        archivesAdapter.set(it, prefsHelper.getDefaultArchiveId())
    }

    private val onChangeDefaultArchiveObserver = Observer<Int> {
        viewModel.changeDefaultArchiveTo(it)
    }

    private val onDefaultArchiveChanged = Observer<Int> {
        archivesAdapter.onDefaultArchiveChanged(it)
    }

    private val onDeleteArchiveObserver = Observer<Archive> { archive ->
        val viewDialog: View = layoutInflater.inflate(R.layout.dialog_delete, null)
        val alert = android.app.AlertDialog.Builder(context)
            .setView(viewDialog)
            .create()
        viewDialog.tvTitle.text = getString(R.string.archive_delete_archive_title)
        viewDialog.btnDelete.setOnClickListener {
            viewModel.deleteArchive(archive)
            alert.dismiss()
        }
        viewDialog.btnCancel.setOnClickListener {
            alert.dismiss()
        }
        alert.show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(ArchiveViewModel::class.java)
        binding = FragmentArchiveBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.btnCurrentArchiveOptions.setOnClickListener(this)
        initPendingArchivesRecyclerView(binding.rvPendingArchives)
        initArchivesRecyclerView(binding.rvArchives)
        dialogCreateArchiveViewModel = ViewModelProvider(this).get(CreateNewArchiveViewModel::class.java)
        archiveTypeAdapter = ArrayAdapter(
            requireContext(),
            R.layout.menu_item_dropdown_access_level,
            archiveTypeList
        )

        return binding.root
    }

    private fun initPendingArchivesRecyclerView(rvPendingArchives: RecyclerView) {
        pendingArchivesRecyclerView = rvPendingArchives
        pendingArchivesAdapter = PendingArchivesAdapter(viewModel)
        pendingArchivesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = pendingArchivesAdapter
        }
    }

    private fun initArchivesRecyclerView(rvArchives: RecyclerView) {
        archivesRecyclerView = rvArchives
        archivesAdapter = ArchivesAdapter(this)
        archivesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = archivesAdapter
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnCurrentArchiveOptions -> {
                val prefsHelper = PreferencesHelper(
                    requireContext().getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                    )
                )

                showArchiveOptions(Archive(prefsHelper.getCurrentArchiveId()))
            }
        }
    }

    override fun onArchiveClick(archive: Archive) {
        viewModel.switchCurrentArchiveTo(archive)
    }

    override fun onOptionsBtnClick(archive: Archive) {
        showArchiveOptions(archive)
    }

    private val onShowCreateArchiveDialog = Observer<Void> {
        dialogCreateArchiveBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_create_new_archive, null, false
        )
        dialogCreateArchiveBinding.executePendingBindings()
        dialogCreateArchiveBinding.lifecycleOwner = this
        dialogCreateArchiveBinding.viewModel = dialogCreateArchiveViewModel
        dialogCreateArchiveViewModel.clearFields()
        dialogCreateArchiveBinding.actvArchiveType.setOnClickListener {
            context?.hideKeyboardFrom(dialogCreateArchiveBinding.root.windowToken)
        }
        dialogCreateArchiveBinding.actvArchiveType.setAdapter(archiveTypeAdapter)
        dialogCreateArchiveBinding.actvArchiveType.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val selectedRole = archiveTypeAdapter.getItem(position) as String
                dialogCreateArchiveViewModel.setArchiveType(
                    ArchiveType.valueOf(
                        selectedRole.uppercase(
                            Locale.getDefault()
                        )))
            }
        val thisContext = context

        if (thisContext != null) {
            alertDialog = AlertDialog.Builder(thisContext)
                .setView(dialogCreateArchiveBinding.root)
                .create()
            dialogCreateArchiveBinding.btnCancel.setOnClickListener {
                alertDialog?.dismiss()
            }
            alertDialog?.show()
        }
    }

    private val onArchiveCreated = Observer<Void> {
        viewModel.refreshArchives()
        alertDialog?.dismiss()
    }

    private fun showArchiveOptions(archive: Archive) {
        archiveOptionsFragment = ArchiveOptionsFragment()
        archiveOptionsFragment?.setBundleArguments(archive)
        archiveOptionsFragment?.show(parentFragmentManager, archiveOptionsFragment?.tag)
        archiveOptionsFragment?.getOnChangeDefaultArchiveRequest()
            ?.observe(this, onChangeDefaultArchiveObserver)
        archiveOptionsFragment?.getOnDeleteArchiveRequest()
            ?.observe(this, onDeleteArchiveObserver)
    }

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onShowMessage)
        viewModel.getOnPendingArchivesRetrieved().observe(this, onPendingArchivesRetrieved)
        viewModel.getOnArchivesRetrieved().observe(this, onArchivesRetrieved)
        viewModel.getOnDefaultArchiveChanged().observe(this, onDefaultArchiveChanged)
        viewModel.getShowCreateArchiveDialog().observe(this, onShowCreateArchiveDialog)
        dialogCreateArchiveViewModel.getShowMessage().observe(this, onShowMessage)
        dialogCreateArchiveViewModel.getOnArchiveCreatedResult().observe(this, onArchiveCreated)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getOnPendingArchivesRetrieved().removeObserver(onPendingArchivesRetrieved)
        viewModel.getOnArchivesRetrieved().removeObserver(onArchivesRetrieved)
        viewModel.getOnDefaultArchiveChanged().removeObserver(onDefaultArchiveChanged)
        viewModel.getShowCreateArchiveDialog().removeObserver(onShowCreateArchiveDialog)
        archiveOptionsFragment?.getOnChangeDefaultArchiveRequest()
            ?.removeObserver(onChangeDefaultArchiveObserver)
        archiveOptionsFragment?.getOnDeleteArchiveRequest()
            ?.removeObserver(onDeleteArchiveObserver)
        dialogCreateArchiveViewModel.getShowMessage().removeObserver(onShowMessage)
        dialogCreateArchiveViewModel.getOnArchiveCreatedResult().removeObserver(onArchiveCreated)
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