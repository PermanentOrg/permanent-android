package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.Editable
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.AccountRepositoryImpl
import org.permanent.permanent.repositories.AuthenticationRepositoryImpl
import org.permanent.permanent.repositories.IAccountRepository
import org.permanent.permanent.repositories.IAuthenticationRepository
import java.util.regex.Pattern


class SignUpViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val nameError = MutableLiveData<Int>()
    private val emailError = MutableLiveData<Int>()
    private val passwordError = MutableLiveData<Int>()
    private val onErrorMessage = MutableLiveData<String>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onLoggedIn = SingleLiveEvent<Void>()
    private val onReadyToShowTermsDialog = SingleLiveEvent<Void>()
    private val onAlreadyHaveAccount = SingleLiveEvent<Void>()
    private val currentName = MutableLiveData<String>()
    private val currentEmail = MutableLiveData<String>()
    private val currentPassword = MutableLiveData<String>()
    private var accountRepository: IAccountRepository = AccountRepositoryImpl(application)
    private var authRepository: IAuthenticationRepository = AuthenticationRepositoryImpl(application)

    fun getCurrentName(): MutableLiveData<String>? {
        return currentName
    }

    fun getCurrentEmail(): MutableLiveData<String>? {
        return currentEmail
    }

    fun getCurrentPassword(): MutableLiveData<String>? {
        return currentPassword
    }

    fun getNameError(): LiveData<Int> {
        return nameError
    }

    fun getEmailError(): LiveData<Int> {
        return emailError
    }

    fun getPasswordError(): LiveData<Int> {
        return passwordError
    }

    fun onNameTextChanged(name: Editable) {
        currentName.value = name.toString()
    }

    fun onEmailTextChanged(email: Editable) {
        currentEmail.value = email.toString()
    }

    fun onPasswordTextChanged(password: Editable) {
        currentPassword.value = password.toString()
    }

    fun getOnErrorMessage(): MutableLiveData<String> {
        return onErrorMessage
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getOnLoggedIn(): MutableLiveData<Void> {
        return onLoggedIn
    }

    fun getOnReadyToShowTermsDialog(): MutableLiveData<Void> {
        return onReadyToShowTermsDialog
    }

    fun getOnAlreadyHaveAccount(): MutableLiveData<Void> {
        return onAlreadyHaveAccount
    }

    fun alreadyHaveAccount() {
        onAlreadyHaveAccount.call()
    }

    fun onSignUpBtnClick() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        if (!isNameValid()) return
        if (!isEmailValid()) return
        if (!isPasswordValid()) return

        onReadyToShowTermsDialog.call()
    }

    fun makeAccount() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        val name = currentName.value
        val email = currentEmail.value
        val password = currentPassword.value

        if (name != null && email != null && password != null) {
            isBusy.value = true
            accountRepository.signUp(name, email, password, object : IResponseListener {

                override fun onSuccess(message: String?) {
                    isBusy.value = false
                    login(email, password)
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    onErrorMessage.value = error
                }
            })
        }
    }

    fun login(email: String, password: String) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        isBusy.value = true
        authRepository.login(email, password, object : IAuthenticationRepository.IOnLoginListener {
            override fun onSuccess() {
                isBusy.value = false
                onLoggedIn.call()
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                onErrorMessage.value = error
            }
        })
    }

    private fun isNameValid(): Boolean {
        currentName.value = currentName.value?.trim()
        val name = currentName.value

        return if (name.isNullOrEmpty()) {
            nameError.value = R.string.sign_up_empty_name_error
            false
        } else {
            nameError.value = null
            true
        }
    }

    private fun isEmailValid(): Boolean {
        currentEmail.value = currentEmail.value?.trim()
        val email = currentEmail.value
        val pattern: Pattern = Patterns.EMAIL_ADDRESS

        if (email.isNullOrEmpty() || !pattern.matcher(email).matches()) {
            emailError.value = R.string.invalid_email_error
            return false
        }
        emailError.value = null
        return true
    }

    private fun isPasswordValid(): Boolean {
        currentPassword.value = currentPassword.value?.trim()
        val password = currentPassword.value

        if (password.isNullOrEmpty()) {
            passwordError.value = R.string.password_empty_error
            return false
        } else {
            if (password.length < Constants.MIN_PASSWORD_LENGTH) {
                passwordError.value = R.string.sign_up_password_too_small_error
                return false
            }
        }
        passwordError.value = null
        return true
    }
}
