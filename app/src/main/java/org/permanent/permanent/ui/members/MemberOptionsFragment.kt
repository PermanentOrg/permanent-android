package org.permanent.permanent.ui.members

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import org.permanent.permanent.databinding.FragmentMemberOptionsBinding
import org.permanent.permanent.models.Account
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.viewmodels.MemberOptionsViewModel
import org.permanent.permanent.viewmodels.SingleLiveEvent

const val PARCELABLE_ACCOUNT_KEY = "parcelable_account_key"

class MemberOptionsFragment : PermanentBottomSheetFragment() {
    private lateinit var binding: FragmentMemberOptionsBinding
    private lateinit var viewModel: MemberOptionsViewModel
    private val onShowEditMemberDialogRequest = MutableLiveData<Account>()
    private val onMemberRemoved = SingleLiveEvent<String>()

    fun setBundleArguments(member: Account) {
        val bundle = Bundle()
        bundle.putParcelable(PARCELABLE_ACCOUNT_KEY, member)
        this.arguments = bundle
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(MemberOptionsViewModel::class.java)
        binding = FragmentMemberOptionsBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        viewModel.setMember(arguments?.getParcelable(PARCELABLE_ACCOUNT_KEY))
        return binding.root
    }

    private val onEditMemberObserver = Observer<Account> { account ->
        dismiss()
        onShowEditMemberDialogRequest.value = account
    }

    private val onMemberRemovedObserver = Observer<String> { message ->
        dismiss()
        onMemberRemoved.value = message
    }

    private val onShowSnackbar = Observer<String> {
        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
    }

    fun getShowEditMemberDialogRequest(): MutableLiveData<Account> = onShowEditMemberDialogRequest

    fun getOnMemberRemoved(): MutableLiveData<String> = onMemberRemoved

    override fun connectViewModelEvents() {
        viewModel.getOnEditMemberRequest().observe(this, onEditMemberObserver)
        viewModel.getOnMemberRemoved().observe(this, onMemberRemovedObserver)
        viewModel.getShowSnackbar().observe(this, onShowSnackbar)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnEditMemberRequest().removeObserver(onEditMemberObserver)
        viewModel.getOnMemberRemoved().removeObserver(onMemberRemovedObserver)
        viewModel.getShowSnackbar().removeObserver(onShowSnackbar)
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