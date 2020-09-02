package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.Editable
import android.text.TextUtils
import android.util.Patterns
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.R
import org.permanent.permanent.repositories.ILoginRepository
import java.util.regex.Pattern

class LoginViewModel(application: Application) : ObservableAndroidViewModel(application) {

    val onError = MutableLiveData<Int>()
    val authenticationError = MutableLiveData<String>()
    private val emailError = MutableLiveData<Int>()
    private val passwordError = MutableLiveData<Int>()
    private val onBiometricAuthSuccess = SingleLiveEvent<Void>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onLoggedIn = SingleLiveEvent<Void>()
    private val onSignUp = SingleLiveEvent<Void>()
    private val onPasswordReset = SingleLiveEvent<Void>()

    private val currentEmail = MutableLiveData<String>()
    private val currentPassword = MutableLiveData<String>()

    private var loginRepository: ILoginRepository? = null

    init {
        //TODO implement login repository
//        loginRepository = FirebaseLoginRepository()
    }

    fun getCurrentEmail(): MutableLiveData<String>? {
        return currentEmail
    }

    fun getCurrentPassword(): MutableLiveData<String>? {
        return currentPassword
    }

    fun emailError(): LiveData<Int> {
        return emailError
    }

    fun passwordError(): LiveData<Int> {
        return passwordError
    }

    fun onEmailTextChanged(email: Editable) {
        currentEmail.value = email.toString().trim { it <= ' ' }
    }

    fun onPasswordTextChanged(password: Editable) {
        currentPassword.value = password.toString().trim { it <= ' ' }
    }

    fun onError(): LiveData<Int> {
        return onError
    }

    fun onAuthenticationError(): LiveData<String>{
        return authenticationError
    }

    fun onBiometricAuthSuccess(): LiveData<Void> {
        return onBiometricAuthSuccess
    }

    fun onIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun onLoggedIn(): MutableLiveData<Void> {
        return onLoggedIn
    }

    fun onSignUp(): MutableLiveData<Void> {
        return onSignUp
    }

    fun onPasswordReset(): MutableLiveData<Void> {
        return onPasswordReset
    }

    fun signUp() {
        onSignUp.call()
    }

    fun forgotPassword() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        val email = currentEmail.value

        if (TextUtils.isEmpty(email)) {
            onError.value = R.string.invalid_email_error
            return
        }

        isBusy.value = true
        loginRepository?.forgotPassword(email, object : ILoginRepository.IOnResetPasswordListener {
            override fun onSuccess() {
                isBusy.value = false
                onPasswordReset.call()
            }

            override fun onFailed(error: String?, errorCode: Int) {
                isBusy.value = false
                authenticationError.value = error
            }
        })
    }

    fun useTouchId() {
        val biometricManager = BiometricManager.from(getApplication())

        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                onBiometricAuthSuccess.call()
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                onError.value = R.string.login_no_biometric_error
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                onError.value = R.string.login_biometric_unavailable_error
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                onError.value = R.string.login_biometric_not_setup_error
        }
    }

    private fun checkEmail(email: String?): Boolean {
        if (email.isNullOrEmpty()) {
            emailError.value = R.string.invalid_email_error
            return false
        } else {
            val pattern: Pattern = Patterns.EMAIL_ADDRESS
            if (!pattern.matcher(email).matches()) {
                emailError.value = R.string.invalid_email_error
                return false
            }

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
        loginRepository?.login(email, password, object : ILoginRepository.IOnLoginListener {
            override fun onSuccess() {
                isBusy.value = false
                onLoggedIn.call()
            }

            override fun onFailed(error: String?, errorCode: Int) {
                isBusy.value = false
                authenticationError.value = error
            }
        })
    }


}