package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.Editable
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import java.util.regex.Pattern

class AddMemberViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private var accessRole: AccessRole? = null
    private val currentEmail = MutableLiveData<String>()
    private val emailError = MutableLiveData<Int>()
    private val accessRoleError = MutableLiveData<Int>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onMemberAdded = SingleLiveEvent<Void>()
    private val showSnackbarSuccess = MutableLiveData<String>()
    private val showSnackbar = MutableLiveData<String>()
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)

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

    fun getOnMemberAdded(): LiveData<Void> {
        return onMemberAdded
    }

    fun getShowSuccessSnackbar(): LiveData<String> {
        return showSnackbarSuccess
    }

    fun getShowSnackbar(): LiveData<String> {
        return showSnackbar
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
            fileRepository.addMember(email, accessRole,
                object : IFileRepository.IOnResponseListener {
                    override fun onSuccess(message: String?) {
                        isBusy.value = false
                        onMemberAdded.call()
                        showSnackbarSuccess.value = message
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
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
