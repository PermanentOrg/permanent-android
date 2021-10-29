package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Account
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
    private val itemName = MutableLiveData<String>()
    private val onEditMemberRequest = SingleLiveEvent<Account>()
    private val onMemberRemoved = SingleLiveEvent<String>()
    private val onShareRemoved = SingleLiveEvent<Share>()
    private val showMessage = MutableLiveData<String>()
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

    fun onEditBtnClick() {
        onEditMemberRequest.value = member
    }

    fun onRemoveBtnClick() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        if (member != null) removeMember() else removeShare()
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
                        showMessage.value = error
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
                    onShareRemoved.value = it
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    showMessage.value = error
                }
            })
        }
    }

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getItemName(): MutableLiveData<String> = itemName

    fun getOnEditMemberRequest(): MutableLiveData<Account> = onEditMemberRequest

    fun getOnMemberRemoved(): LiveData<String> = onMemberRemoved

    fun getOnShareRemoved(): LiveData<Share> = onShareRemoved

    fun getShowMessage(): LiveData<String> = showMessage
}
