package org.permanent.permanent.ui.invitations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import org.permanent.permanent.R
import org.permanent.permanent.databinding.DialogSendInvitationBinding
import org.permanent.permanent.databinding.FragmentInvitationsBinding
import org.permanent.permanent.models.Invitation
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.InvitationsViewModel
import org.permanent.permanent.viewmodels.SendInvitationViewModel

class InvitationsFragment : PermanentBaseFragment() {

    private lateinit var binding: FragmentInvitationsBinding
    private lateinit var viewModel: InvitationsViewModel
    private lateinit var sendInvitationViewModel: SendInvitationViewModel
    private lateinit var sendInvitationBinding: DialogSendInvitationBinding
    private lateinit var invitationsRecyclerView: RecyclerView
    private lateinit var invitationsAdapter: InvitationsAdapter
    private var alertDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(InvitationsViewModel::class.java)
        binding = FragmentInvitationsBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        sendInvitationViewModel = ViewModelProvider(this).get(SendInvitationViewModel::class.java)
        initRecyclerView(binding.rvInvitations)

        return binding.root
    }

    private val onShowSnackbar = Observer<String> {
        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
    }

    private val onInvitesRetrieved = Observer<MutableList<Invitation>> {
        invitationsAdapter.set(it)
    }

    private val onShowSendInvitationDialog = Observer<Void> {
        sendInvitationBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_send_invitation, null, false
        )
        sendInvitationBinding.executePendingBindings()
        sendInvitationBinding.lifecycleOwner = this
        sendInvitationBinding.viewModel = sendInvitationViewModel
        alertDialog = AlertDialog.Builder(requireContext())
            .setView(sendInvitationBinding.root)
            .create()
        sendInvitationBinding.btnCancel.setOnClickListener {
            alertDialog?.dismiss()
        }
        alertDialog?.show()
    }

    private val onInvitationSent = Observer<Void> {
        viewModel.refreshInvitations()
        alertDialog?.dismiss()
    }

    private fun initRecyclerView(recyclerView: RecyclerView) {
        invitationsRecyclerView = recyclerView
        invitationsAdapter = InvitationsAdapter(viewModel)
        invitationsRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = invitationsAdapter
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        }
    }

    override fun connectViewModelEvents() {
        viewModel.getShowSnackbarMessage().observe(this, onShowSnackbar)
        viewModel.getOnInvitesRetrieved().observe(this, onInvitesRetrieved)
        viewModel.getShowSendInvitationDialog().observe(this, onShowSendInvitationDialog)
        sendInvitationViewModel.getOnInvitationSent().observe(this, onInvitationSent)
        sendInvitationViewModel.getShowSnackbar().observe(this, onShowSnackbar)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowSnackbarMessage().removeObserver(onShowSnackbar)
        viewModel.getOnInvitesRetrieved().removeObserver(onInvitesRetrieved)
        viewModel.getShowSendInvitationDialog().removeObserver(onShowSendInvitationDialog)
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
