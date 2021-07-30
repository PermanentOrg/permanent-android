package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.Validator
import org.permanent.permanent.repositories.AuthenticationRepositoryImpl
import org.permanent.permanent.repositories.IAuthenticationRepository

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
    val versionName = MutableLiveData(application.getString(
        R.string.version_text, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE.toString()))
    private var authRepository: IAuthenticationRepository = AuthenticationRepositoryImpl(application)

    fun login() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        currentEmail.value = currentEmail.value?.trim()
        currentPassword.value = currentPassword.value?.trim()

        val email = currentEmail.value
        val password = currentPassword.value

        if (!Validator.isValidEmail(email, emailError)) return
        if (!Validator.isValidPassword(password, passwordError)) return

        isBusy.value = true
        authRepository.login(email!!, password!!, object : IAuthenticationRepository.IOnLoginListener {
            override fun onSuccess() {
                isBusy.value = false
                onLoggedIn.call()
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                when (error) {
                    Constants.ERROR_UNKNOWN_SIGNIN -> errorStringId.value =
                        R.string.login_bad_credentials
                    Constants.ERROR_SERVER_ERROR -> errorStringId.value =
                        R.string.server_error
                    else -> errorMessage.value = error
                }
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
                when (error) {
                    Constants.ERROR_SERVER_ERROR -> errorStringId.value =
                        R.string.server_error
                    else -> errorMessage.value = error
                }
            }
        })
    }

    fun getCurrentEmail(): MutableLiveData<String> = currentEmail

    fun getCurrentPassword(): MutableLiveData<String> = currentPassword

    fun getEmailError(): LiveData<Int> = emailError

    fun getPasswordError(): LiveData<Int> = passwordError

    fun onEmailTextChanged(email: Editable) {
        currentEmail.value = email.toString()
    }

    fun onPasswordTextChanged(password: Editable) {
        currentPassword.value = password.toString()
    }

    fun getErrorStringId(): LiveData<Int> = errorStringId

    fun getErrorMessage(): LiveData<String> = errorMessage

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getOnLoggedIn(): MutableLiveData<Void> = onLoggedIn

    fun getOnSignUp(): MutableLiveData<Void> = onSignUp

    fun getOnPasswordReset(): MutableLiveData<Void> = onPasswordReset

    fun getOnReadyToShowForgotPassDialog(): MutableLiveData<Void> = onReadyToShowForgotPassDialog
}
