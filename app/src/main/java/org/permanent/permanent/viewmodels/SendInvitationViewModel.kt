package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.Editable
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.IInvitationRepository
import org.permanent.permanent.repositories.InvitationRepositoryImpl
import java.util.regex.Pattern

class SendInvitationViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val currentEmail = MutableLiveData<String>()
    private val currentName = MutableLiveData<String>()
    private val emailError = MutableLiveData<Int>()
    private val nameError = MutableLiveData<Int>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onInvitationSent = SingleLiveEvent<Void>()
    private val showSnackbar = MutableLiveData<String>()
    private var invitationRepository: IInvitationRepository = InvitationRepositoryImpl(application)

    fun getCurrentEmail(): MutableLiveData<String> {
        return currentEmail
    }

    fun getCurrentName(): LiveData<String> {
        return currentName
    }

    fun getEmailError(): LiveData<Int> {
        return emailError
    }

    fun getNameError(): LiveData<Int> {
        return nameError
    }

    fun onEmailTextChanged(email: Editable) {
        currentEmail.value = email.toString()
    }

    fun onNameTextChanged(name: Editable) {
        currentName.value = name.toString()
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getOnInvitationSent(): LiveData<Void> {
        return onInvitationSent
    }

    fun getShowSnackbar(): LiveData<String> {
        return showSnackbar
    }

    fun sendInvitation() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        val name = getValidatedName()
        val email = getValidatedEmail()

        if (email != null && name != null) {
            isBusy.value = true
            invitationRepository.sendInvitation(name, email, object : IResponseListener {

                override fun onSuccess(message: String?) {
                    isBusy.value = false
                    showSnackbar.value = message
                    onInvitationSent.call()
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

    private fun getValidatedName(): String? {
        val name = currentName.value?.trim()
        if (name.isNullOrEmpty()) {
            nameError.value = R.string.invalid_name_error
            return null
        }
        nameError.value = null
        return name
    }
}
