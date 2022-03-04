package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Account
import org.permanent.permanent.models.Milestone
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
    private var milestone: Milestone? = null
    private val itemName = MutableLiveData<String>()
    private val onEditShareRequest = SingleLiveEvent<Share>()
    private val onEditMemberRequest = SingleLiveEvent<Account>()
    private val onEditOnlinePresenceRequest = SingleLiveEvent<ProfileItem>()
    private val onDeleteOnlinePresenceRequest = SingleLiveEvent<ProfileItem>()
    private val onEditMilestoneRequest = SingleLiveEvent<Milestone>()
    private val onDeleteMilestoneRequest = SingleLiveEvent<Milestone>()
    private val onMemberRemoved = SingleLiveEvent<String>()
    private val onShareRemoved = SingleLiveEvent<Share>()
    private val showSnackbar = MutableLiveData<String>()
    private val showSnackbarSuccess = MutableLiveData<String>()
    private var archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)
    private var shareRepository: IShareRepository = ShareRepositoryImpl(application)

    fun setMember(member: Account?) {
        this.member = member
        member?.primaryEmail?.let { itemName.value = it }
    }

    fun setShare(share: Share?) {
        this.share = share
        share?.archive?.fullName?.let { itemName.value = it }
    }

    fun setProfileItem(profileItem: ProfileItem?) {
        this.profileItem = profileItem
        profileItem?.string1?.let {
            itemName.value = it
        }
    }

    fun setMilestone(milestone: Milestone?) {
        this.milestone = milestone
        milestone?.title?.let {
            itemName.value = it
        }
    }

    fun onEditBtnClick() {
        when {
            member != null -> onEditMemberRequest.value = member
            share != null -> onEditShareRequest.value = share
            profileItem != null -> onEditOnlinePresenceRequest.value = profileItem
            else -> onEditMilestoneRequest.value = milestone
        }
    }

    fun onRemoveBtnClick() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        when {
            member != null -> removeMember()
            share != null -> removeShare()
            profileItem != null -> onDeleteOnlinePresenceRequest.value = profileItem
            else -> onDeleteMilestoneRequest.value = milestone
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

    private fun removeShare() {
        share?.let { share ->
            isBusy.value = true
            shareRepository.deleteShare(share, object : IResponseListener {
                override fun onSuccess(message: String?) {
                    isBusy.value = false
                    message?.let { showSnackbarSuccess.value = it }
                    onShareRemoved.value = share
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

    fun getOnEditShareRequest(): MutableLiveData<Share> = onEditShareRequest

    fun getOnEditOnlinePresenceRequest(): MutableLiveData<ProfileItem> = onEditOnlinePresenceRequest

    fun getOnDeleteOnlinePresenceRequest(): MutableLiveData<ProfileItem> =
        onDeleteOnlinePresenceRequest

    fun getOnEditMilestoneRequest(): MutableLiveData<Milestone> = onEditMilestoneRequest

    fun getOnDeleteMilestoneRequest(): MutableLiveData<Milestone> =
        onDeleteMilestoneRequest

    fun getOnMemberRemoved(): LiveData<String> = onMemberRemoved

    fun getOnShareRemoved(): LiveData<Share> = onShareRemoved

    fun getShowSnackbarRequest(): LiveData<String> = showSnackbar

    fun getShowSnackbarSuccessRequest(): LiveData<String> = showSnackbarSuccess
}
