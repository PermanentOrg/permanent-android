package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Account
import org.permanent.permanent.models.ProfileItem
import org.permanent.permanent.models.Share
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.ArchiveRepositoryImpl
import org.permanent.permanent.repositories.IArchiveRepository
import org.permanent.permanent.repositories.IShareRepository
import org.permanent.permanent.repositories.ShareRepositoryImpl

class ItemOptionsViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val isBusy = MutableLiveData<Boolean>()
    private var member: Account? = null
    private var share: Share? = null
    private var profileItem: ProfileItem? = null
    private val itemName = MutableLiveData<String>()
    private val onEditShareRequest = SingleLiveEvent<Share>()
    private val onEditMemberRequest = SingleLiveEvent<Account>()
    private val onEditOnlinePresenceRequest = SingleLiveEvent<ProfileItem>()
    private val onDeleteOnlinePresenceRequest = SingleLiveEvent<ProfileItem>()
    private val onMemberRemoved = SingleLiveEvent<String>()
    private val onShareRemoved = SingleLiveEvent<Share>()
    private val showSnackbar = MutableLiveData<String>()
    private val showSnackbarSuccess = MutableLiveData<String>()
    private var archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)
    private var shareRepository: IShareRepository = ShareRepositoryImpl(application)

    fun setMember(member: Account?) {
        this.member = member
        itemName.value = member?.primaryEmail
    }

    fun setShare(share: Share?) {
        this.share = share
        itemName.value = share?.archive?.fullName
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
            share != null -> onEditShareRequest.value = share
            else -> onEditOnlinePresenceRequest.value = profileItem
        }
    }

    fun onRemoveBtnClick() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        when {
            member != null -> removeMember()
            share != null -> removeShare()
            else -> onDeleteOnlinePresenceRequest.value = profileItem
        }
    }

    private fun removeMember() {
        if (member?.id != null && member?.primaryEmail != null && member?.accessRole != null) {
            isBusy.value = true
            archiveRepository.deleteMember(member!!.id!!, member!!.primaryEmail!!,
                object : IResponseListener {
                    override fun onSuccess(message: String?) {
                        isBusy.value = false
                        onMemberRemoved.value = message
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        showSnackbar.value = error
                    }
                })
        }
    }

    private fun removeShare() {
        share?.let {
            isBusy.value = true
            shareRepository.deleteShare(it, object : IResponseListener {
                override fun onSuccess(message: String?) {
                    isBusy.value = false
                    showSnackbarSuccess.value = message
                    onShareRemoved.value = it
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    showSnackbar.value = error
                }
            })
        }
    }

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getItemName(): MutableLiveData<String> = itemName

    fun getOnEditMemberRequest(): MutableLiveData<Account> = onEditMemberRequest

    fun getOnEditShareRequest(): MutableLiveData<Share> = onEditShareRequest

    fun getOnEditOnlinePresenceRequest(): MutableLiveData<ProfileItem> = onEditOnlinePresenceRequest

    fun getOnDeleteOnlinePresenceRequest(): MutableLiveData<ProfileItem> = onDeleteOnlinePresenceRequest

    fun getOnMemberRemoved(): LiveData<String> = onMemberRemoved

    fun getOnShareRemoved(): LiveData<Share> = onShareRemoved

    fun getShowSnackbarRequest(): LiveData<String> = showSnackbar

    fun getShowSnackbarSuccessRequest(): LiveData<String> = showSnackbarSuccess
}
