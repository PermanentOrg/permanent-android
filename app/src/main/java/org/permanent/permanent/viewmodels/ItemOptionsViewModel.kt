package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Account
import org.permanent.permanent.models.ProfileItem
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.ArchiveRepositoryImpl
import org.permanent.permanent.repositories.IArchiveRepository

class ItemOptionsViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val isBusy = MutableLiveData<Boolean>()
    private var member: Account? = null
    private var profileItem: ProfileItem? = null
    private val itemName = MutableLiveData<String>()
    private val onEditMemberRequest = SingleLiveEvent<Account>()
    private val onEditProfileItemRequest = SingleLiveEvent<ProfileItem>()
    private val onDeleteProfileItemRequest = SingleLiveEvent<ProfileItem>()
    private val onMemberRemoved = SingleLiveEvent<String>()
    private val showSnackbar = MutableLiveData<String>()
    private val showSnackbarSuccess = MutableLiveData<String>()
    private var archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)

    fun setMember(member: Account?) {
        this.member = member
        member?.primaryEmail?.let { itemName.value = it }
    }

    fun setProfileItem(profileItem: ProfileItem?) {
        this.profileItem = profileItem
        profileItem?.string1?.let {
            itemName.value = it
        }
    }

    fun onEditBtnClick() {
        when {
            member != null -> onEditMemberRequest.value = member
            else -> onEditProfileItemRequest.value = profileItem
        }
    }

    fun onDeleteBtnClick() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        when {
            member != null -> removeMember()
            else -> onDeleteProfileItemRequest.value = profileItem
        }
    }

    private fun removeMember() {
        if (member?.id != null && member?.primaryEmail != null && member?.accessRole != null) {
            isBusy.value = true
            archiveRepository.deleteMember(member!!.id!!, member!!.primaryEmail!!,
                object : IResponseListener {
                    override fun onSuccess(message: String?) {
                        isBusy.value = false
                        message?.let { onMemberRemoved.value = it }
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        error?.let { showSnackbar.value = it }
                    }
                })
        }
    }

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getItemName(): MutableLiveData<String> = itemName

    fun getOnEditMemberRequest(): MutableLiveData<Account> = onEditMemberRequest

    fun getOnEditProfileItemRequest(): MutableLiveData<ProfileItem> = onEditProfileItemRequest

    fun getOnDeleteProfileItemRequest(): MutableLiveData<ProfileItem> = onDeleteProfileItemRequest

    fun getOnMemberRemoved(): LiveData<String> = onMemberRemoved

    fun getShowSnackbarRequest(): LiveData<String> = showSnackbar

    fun getShowSnackbarSuccessRequest(): LiveData<String> = showSnackbarSuccess
}
