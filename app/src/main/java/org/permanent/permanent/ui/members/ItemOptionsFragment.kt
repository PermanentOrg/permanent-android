package org.permanent.permanent.ui.members

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentItemOptionsBinding
import org.permanent.permanent.models.Account
import org.permanent.permanent.models.ProfileItem
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.ui.public.PublicProfileFragment.Companion.PARCELABLE_PROFILE_ITEM_KEY
import org.permanent.permanent.viewmodels.ItemOptionsViewModel
import org.permanent.permanent.viewmodels.SingleLiveEvent

const val PARCELABLE_ACCOUNT_KEY = "parcelable_account_key"

class ItemOptionsFragment : PermanentBottomSheetFragment() {
    private lateinit var binding: FragmentItemOptionsBinding
    private lateinit var viewModel: ItemOptionsViewModel
    private val onShowEditMemberDialogRequest = MutableLiveData<Account>()
    private val onEditProfileItemRequest = MutableLiveData<ProfileItem>()
    private val onDeleteProfileItemRequest = MutableLiveData<ProfileItem>()
    private val onMemberRemoved = SingleLiveEvent<String>()
    private val onShowSnackbar = SingleLiveEvent<String>()
    private val onShowSnackbarSuccess = SingleLiveEvent<String>()

    fun setBundleArguments(member: Account) {
        val bundle = Bundle()
        bundle.putParcelable(PARCELABLE_ACCOUNT_KEY, member)
        this.arguments = bundle
    }

    fun setBundleArguments(profileItem: ProfileItem) {
        val bundle = Bundle()
        bundle.putParcelable(PARCELABLE_PROFILE_ITEM_KEY, profileItem)
        this.arguments = bundle
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[ItemOptionsViewModel::class.java]
        binding = FragmentItemOptionsBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.setMember(arguments?.getParcelable(PARCELABLE_ACCOUNT_KEY))
        val profileItem: ProfileItem? = arguments?.getParcelable(PARCELABLE_PROFILE_ITEM_KEY)
        viewModel.setProfileItem(profileItem)
        if (profileItem != null) binding.btnDelete.setText(R.string.delete)

        return binding.root
    }

    private val onEditMemberObserver = Observer<Account> { account ->
        onShowEditMemberDialogRequest.value = account
        dismiss()
    }

    private val onEditProfileItemObserver = Observer<ProfileItem> { profileItem ->
        onEditProfileItemRequest.value = profileItem
        dismiss()
    }

    private val onDeleteProfileItemObserver = Observer<ProfileItem> { profileItem ->
        onDeleteProfileItemRequest.value = profileItem
        dismiss()
    }

    private val onMemberRemovedObserver = Observer<String> { message ->
        onMemberRemoved.value = message
        dismiss()
    }

    private val onShowSnackbarSuccessObserver = Observer<String> {
        onShowSnackbarSuccess.value = it
    }

    private val onShowSnackbarObserver = Observer<String> {
        onShowSnackbar.value = it
    }

    fun getShowEditMemberDialogRequest(): MutableLiveData<Account> = onShowEditMemberDialogRequest

    fun getEditProfileItemRequest(): MutableLiveData<ProfileItem> = onEditProfileItemRequest

    fun getDeleteProfileItemRequest(): MutableLiveData<ProfileItem> = onDeleteProfileItemRequest

    fun getOnMemberRemoved(): MutableLiveData<String> = onMemberRemoved

    fun getShowSnackbar(): MutableLiveData<String> = onShowSnackbar

    fun getShowSnackbarSuccess(): MutableLiveData<String> = onShowSnackbarSuccess

    override fun connectViewModelEvents() {
        viewModel.getOnEditMemberRequest().observe(this, onEditMemberObserver)
        viewModel.getOnEditProfileItemRequest().observe(this, onEditProfileItemObserver)
        viewModel.getOnDeleteProfileItemRequest().observe(this, onDeleteProfileItemObserver)
        viewModel.getOnMemberRemoved().observe(this, onMemberRemovedObserver)
        viewModel.getShowSnackbarRequest().observe(this, onShowSnackbarObserver)
        viewModel.getShowSnackbarSuccessRequest().observe(this, onShowSnackbarSuccessObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnEditMemberRequest().removeObserver(onEditMemberObserver)
        viewModel.getOnEditProfileItemRequest().removeObserver(onEditProfileItemObserver)
        viewModel.getOnDeleteProfileItemRequest().removeObserver(onDeleteProfileItemObserver)
        viewModel.getOnMemberRemoved().removeObserver(onMemberRemovedObserver)
        viewModel.getShowSnackbarRequest().removeObserver(onShowSnackbarObserver)
        viewModel.getShowSnackbarSuccessRequest().removeObserver(onShowSnackbarSuccessObserver)
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