package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Account
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.ArchiveRepositoryImpl
import org.permanent.permanent.repositories.IArchiveRepository

class EditMemberViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private var member: Account? = null
    private val fullName = MutableLiveData<String>()
    private val email = MutableLiveData<String>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onMemberEdited = SingleLiveEvent<Void>()
    private val onOwnershipTransferRequest = SingleLiveEvent<Boolean>()
    private val onMemberDeleted = SingleLiveEvent<Void>()
    private val showSnackbarSuccess = MutableLiveData<String>()
    private val showSnackbar = MutableLiveData<String>()
    private var archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)

    fun setMember(member: Account?) {
        this.member = member
        fullName.value = member?.fullName
        email.value = member?.primaryEmail
    }

    fun setAccessLevel(role: AccessRole) {
        member?.accessRole = role
    }

    fun onSaveBtnClick() {
        if (member?.accessRole == AccessRole.OWNER) {
            onOwnershipTransferRequest.value = false
        } else {
            updateAccessRole()
        }
    }

    private fun updateAccessRole() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        val memberId = member?.id
        val memberEmail = member?.primaryEmail
        val memberAccessRole = member?.accessRole

        if (memberId != null && memberEmail != null && memberAccessRole != null) {
            isBusy.value = true
            archiveRepository.updateMember(memberId, memberEmail, memberAccessRole,
                object : IResponseListener {
                    override fun onSuccess(message: String?) {
                        isBusy.value = false
                        onMemberEdited.call()
                        showSnackbarSuccess.value = message
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        showSnackbar.value = error
                    }
                })
        }
    }

    fun transferOwnership() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        val memberEmail = member?.primaryEmail

        if (memberEmail != null) {
            isBusy.value = true
            archiveRepository.transferOwnership(memberEmail, object : IResponseListener {
                override fun onSuccess(message: String?) {
                    isBusy.value = false
                    onMemberEdited.call()
                    showSnackbarSuccess.value = message
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    showSnackbar.value = error
                }
            })
        }
    }

    fun deleteMember() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        if (member?.id != null && member?.primaryEmail != null && member?.accessRole != null) {
            isBusy.value = true
            archiveRepository.deleteMember(member!!.id!!, member!!.primaryEmail!!,
                object : IResponseListener {
                    override fun onSuccess(message: String?) {
                        isBusy.value = false
                        onMemberDeleted.call()
                        showSnackbarSuccess.value = message
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        showSnackbar.value = error
                    }
                })
        }
    }

    fun getFullName(): MutableLiveData<String> = fullName

    fun getEmail(): MutableLiveData<String> = email

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getOnMemberEdited(): LiveData<Void> = onMemberEdited

    fun getOnOwnershipTransferRequest(): LiveData<Boolean> = onOwnershipTransferRequest

    fun getOnMemberDeleted(): LiveData<Void> = onMemberDeleted

    fun getShowSuccessSnackbar(): LiveData<String> = showSnackbarSuccess

    fun getShowSnackbar(): LiveData<String> = showSnackbar
}
