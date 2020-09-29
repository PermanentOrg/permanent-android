package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.Editable
import android.text.TextUtils
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.repositories.ISignUpRepository
import org.permanent.permanent.repositories.SignUpRepositoryImpl
import java.util.regex.Pattern


class SignUpViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val nameError = MutableLiveData<Int>()
    private val emailError = MutableLiveData<Int>()
    private val passwordError = MutableLiveData<Int>()
    private val onErrorMessage = MutableLiveData<String>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onSignedUp = SingleLiveEvent<Void>()
    private val onReadyToShowTermsDialog = SingleLiveEvent<Void>()
    private val onAlreadyHaveAccount = SingleLiveEvent<Void>()

    private val currentName = MutableLiveData<String>()
    private val currentEmail = MutableLiveData<String>()
    private val currentPassword = MutableLiveData<String>()
    private var signUpRepository: ISignUpRepository = SignUpRepositoryImpl(application)

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
        currentName.value = name.toString().trim { it <= ' ' }
    }

    fun onEmailTextChanged(email: Editable) {
        currentEmail.value = email.toString().trim { it <= ' ' }
    }

    fun onPasswordTextChanged(password: Editable) {
        currentPassword.value = password.toString().trim { it <= ' ' }
    }

    fun getOnErrorMessage(): MutableLiveData<String> {
        return onErrorMessage
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getOnSignedUp(): MutableLiveData<Void> {
        return onSignedUp
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

    private fun checkName(name: String?): Boolean {
        return if (TextUtils.isEmpty(name)) {
            nameError.value = R.string.sign_up_empty_name_error
            false
        } else {
            nameError.value = null
            true
        }
    }

    private fun checkEmail(email: String?): Boolean {
        val pattern: Pattern = Patterns.EMAIL_ADDRESS
        if (email.isNullOrEmpty() || !pattern.matcher(email).matches()) {
            emailError.value = R.string.invalid_email_error
            return false
        }
        emailError.value = null
        return true
    }

    private fun checkEmptyPassword(password: String?): Boolean {
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

    fun onSignUpBtnClick() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        val name = currentName.value
        val email = currentEmail.value
        val password = currentPassword.value

        if (!checkName(name)) return
        if (!checkEmail(email)) return
        if (!checkEmptyPassword(password)) return

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
            signUpRepository.signUp(
                name,
                email,
                password,
                object : ISignUpRepository.IOnSignUpListener {
                    override fun onSuccess() {
                        isBusy.value = false
                        onSignedUp.call()
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        onErrorMessage.value = error
                    }
                })
        }
    }
}
