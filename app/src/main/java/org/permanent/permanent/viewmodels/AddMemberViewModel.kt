package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.Editable
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.ArchiveRepositoryImpl
import org.permanent.permanent.repositories.IArchiveRepository
import java.util.regex.Pattern

class AddMemberViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private var accessRole: AccessRole? = null
    private val currentEmail = MutableLiveData<String>()
    private val emailError = MutableLiveData<Int>()
    private val accessRoleError = MutableLiveData<Int>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onOwnershipTransferRequest = SingleLiveEvent<Boolean>()
    private val onMemberAddedConclusion = SingleLiveEvent<Void?>()
    private val showSnackbarSuccess = MutableLiveData<String>()
    private val showErrorSnackbar = MutableLiveData<String>()
    private var archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)

    fun onEmailTextChanged(email: Editable) {
        currentEmail.value = email.toString()
    }

    fun clearFields() {
        currentEmail.value = ""
        accessRole = null
    }

    fun setAccessRole(role: AccessRole) {
        accessRole = role
        accessRoleError.value = null
    }

    fun onSaveBtnClick() {
        if (accessRole == AccessRole.OWNER) {
            onOwnershipTransferRequest.value = true
        } else {
            addNewMember()
        }
    }

    private fun addNewMember() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        val email = getValidatedEmail()
        val accessRole = getValidatedAccessRole()

        if (email != null && accessRole != null) {
            isBusy.value = true
            archiveRepository.addMember(email, accessRole, object : IResponseListener {
                override fun onSuccess(message: String?) {
                    isBusy.value = false
                    onMemberAddedConclusion.call()
                    showSnackbarSuccess.value = message
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    onMemberAddedConclusion.call()
                    showErrorSnackbar.value = error
                }
            })
        }
    }

    fun transferOwnership() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        val email = getValidatedEmail()

        if (email != null) {
            isBusy.value = true
            archiveRepository.transferOwnership(email, object : IResponseListener {
                override fun onSuccess(message: String?) {
                    isBusy.value = false
                    onMemberAddedConclusion.call()
                    showSnackbarSuccess.value = message
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    onMemberAddedConclusion.call()
                    showErrorSnackbar.value = error
                }
            })
        }
    }

    private fun getValidatedEmail(): String? {
        val pattern: Pattern = Patterns.EMAIL_ADDRESS
        val email = currentEmail.value
        if (email.isNullOrEmpty() || !pattern.matcher(email).matches()) {
            emailError.value = R.string.invalid_email_error
            return null
        }
        emailError.value = null
        return email
    }

    private fun getValidatedAccessRole(): AccessRole? {
        if (accessRole == null) {
            accessRoleError.value = R.string.invalid_access_level_error
            return null
        }
        accessRoleError.value = null
        return accessRole
    }

    fun getCurrentEmail(): MutableLiveData<String> = currentEmail

    fun getEmailError(): LiveData<Int> = emailError

    fun getAccessRoleError(): LiveData<Int> = accessRoleError

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getOnOwnershipTransferRequest(): LiveData<Boolean> = onOwnershipTransferRequest

    fun getOnMemberAddedConclusion(): LiveData<Void?> = onMemberAddedConclusion

    fun getShowSuccessSnackbar(): LiveData<String> = showSnackbarSuccess

    fun getShowErrorSnackbar(): LiveData<String> = showErrorSnackbar
}
