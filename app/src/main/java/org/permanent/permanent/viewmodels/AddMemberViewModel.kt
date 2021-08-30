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
    private val onMemberAddedConclusion = SingleLiveEvent<Void>()
    private val showSnackbarSuccess = MutableLiveData<String>()
    private val showSnackbar = MutableLiveData<String>()
    private var archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)

    fun getCurrentEmail(): MutableLiveData<String> {
        return currentEmail
    }

    fun getEmailError(): LiveData<Int> {
        return emailError
    }

    fun getAccessRoleError(): LiveData<Int> {
        return accessRoleError
    }

    fun onEmailTextChanged(email: Editable) {
        currentEmail.value = email.toString()
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getOnMemberAddedConclusion(): LiveData<Void> {
        return onMemberAddedConclusion
    }

    fun getShowSuccessSnackbar(): LiveData<String> {
        return showSnackbarSuccess
    }

    fun getShowSnackbar(): LiveData<String> {
        return showSnackbar
    }

    fun clearFields() {
        currentEmail.value = ""
        accessRole = null
    }

    fun setAccessRole(role: AccessRole) {
        accessRole = role
    }

    fun addNewMember() {
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
                    showSnackbar.value = error
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
}
