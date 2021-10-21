package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Account
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.ArchiveRepositoryImpl
import org.permanent.permanent.repositories.IArchiveRepository

class MemberOptionsViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val isBusy = MutableLiveData<Boolean>()
    private var member: Account? = null
    private val memberEmail = MutableLiveData<String>()
    private val onEditMemberRequest = SingleLiveEvent<Account>()
    private val onMemberRemoved = SingleLiveEvent<String>()
    private val showSnackbar = MutableLiveData<String>()
    private var archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)

    fun setMember(member: Account?) {
        this.member = member
        memberEmail.value = member?.primaryEmail
    }

    fun onEditBtnClick() {
        onEditMemberRequest.value = member
    }

    fun onRemoveBtnClick() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

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

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getMemberEmail(): MutableLiveData<String> = memberEmail

    fun getOnEditMemberRequest(): MutableLiveData<Account> = onEditMemberRequest

    fun getOnMemberRemoved(): LiveData<String> = onMemberRemoved

    fun getShowSnackbar(): LiveData<String> = showSnackbar
}
