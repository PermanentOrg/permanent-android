package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Account
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.IMemberRepository
import org.permanent.permanent.repositories.MemberRepositoryImpl

class EditMemberViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private var member: Account? = null
    private val fullName = MutableLiveData<String>()
    private val email = MutableLiveData<String>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onMemberEdited = SingleLiveEvent<Void>()
    private val onMemberDeleted = SingleLiveEvent<Void>()
    private val showSnackbarSuccess = MutableLiveData<String>()
    private val showSnackbar = MutableLiveData<String>()
    private var memberRepository: IMemberRepository = MemberRepositoryImpl(application)

    fun setMember(member: Account?) {
        this.member = member
        fullName.value = member?.fullName
        email.value = member?.primaryEmail
    }

    fun getFullName(): MutableLiveData<String> {
        return fullName
    }

    fun getEmail(): MutableLiveData<String> {
        return email
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getOnMemberEdited(): LiveData<Void> {
        return onMemberEdited
    }

    fun getOnMemberDeleted(): LiveData<Void> {
        return onMemberDeleted
    }

    fun getShowSuccessSnackbar(): LiveData<String> {
        return showSnackbarSuccess
    }

    fun getShowSnackbar(): LiveData<String> {
        return showSnackbar
    }

    fun setAccessLevel(role: AccessRole) {
        member?.accessRole = role
    }

    fun saveEdits() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        if (member?.id != null && member?.primaryEmail != null && member?.accessRole != null) {
            isBusy.value = true
            memberRepository.updateMember(member!!.id!!, member!!.primaryEmail!!,
                member!!.accessRole!!, object : IResponseListener {
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
            memberRepository.deleteMember(member!!.id!!, member!!.primaryEmail!!,
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
}
