package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.Editable
import android.text.TextUtils
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.repositories.AuthenticationRepositoryImpl
import org.permanent.permanent.repositories.IAuthenticationRepository
import java.util.regex.Pattern

class LoginFragmentViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val errorMessage = MutableLiveData<String>()
    private val errorStringId = MutableLiveData<Int>()
    private val emailError = MutableLiveData<Int>()
    private val passwordError = MutableLiveData<Int>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onLoggedIn = SingleLiveEvent<Void>()
    private val onSignUp = SingleLiveEvent<Void>()
    private val onPasswordReset = SingleLiveEvent<Void>()
    private val onReadyToShowForgotPassDialog = SingleLiveEvent<Void>()
    private val currentEmail = MutableLiveData<String>()
    private val currentPassword = MutableLiveData<String>()
    private var authRepository: IAuthenticationRepository = AuthenticationRepositoryImpl(application)

    fun getCurrentEmail(): MutableLiveData<String>? {
        return currentEmail
    }

    fun getCurrentPassword(): MutableLiveData<String>? {
        return currentPassword
    }

    fun getEmailError(): LiveData<Int> {
        return emailError
    }

    fun getPasswordError(): LiveData<Int> {
        return passwordError
    }

    fun onEmailTextChanged(email: Editable) {
        currentEmail.value = email.toString().trim { it <= ' ' }
    }

    fun onPasswordTextChanged(password: Editable) {
        currentPassword.value = password.toString().trim { it <= ' ' }
    }

    fun getErrorStringId(): LiveData<Int> {
        return errorStringId
    }

    fun getErrorMessage(): LiveData<String> {
        return errorMessage
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getOnLoggedIn(): MutableLiveData<Void> {
        return onLoggedIn
    }

    fun getOnSignUp(): MutableLiveData<Void> {
        return onSignUp
    }

    fun getOnPasswordReset(): MutableLiveData<Void> {
        return onPasswordReset
    }

    fun getOnReadyToShowForgotPassDialog(): MutableLiveData<Void> {
        return onReadyToShowForgotPassDialog
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

    private fun checkPassword(password: String?): Boolean {
        if (TextUtils.isEmpty(password)) {
            passwordError.value = R.string.login_password_error
            return false
        }
        passwordError.value = null
        return true
    }

    fun login() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        val email = currentEmail.value
        val password = currentPassword.value

        if (!checkEmail(email)) return
        if (!checkPassword(password)) return

        isBusy.value = true
        authRepository.login(email!!, password!!, object : IAuthenticationRepository.IOnLoginListener {
            override fun onSuccess() {
                isBusy.value = false
                onLoggedIn.call()
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                errorMessage.value = error
            }
        })
    }

    fun signUp() {
        onSignUp.call()
    }

    fun onForgotPasswordBtnClick() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        onReadyToShowForgotPassDialog.call()
    }

    fun resetPassword(email: String) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        isBusy.value = true
        authRepository.forgotPassword(email, object : IAuthenticationRepository.IOnResetPasswordListener {
            override fun onSuccess() {
                isBusy.value = false
                onPasswordReset.call()
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                errorMessage.value = error
            }
        })
    }
}
