package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.Editable
import android.text.TextUtils
import android.util.Patterns
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.repositories.ILoginRepository
import java.util.regex.Pattern

class LoginViewModel(application: Application) : ObservableAndroidViewModel(application) {

    val onError = MutableLiveData<String>()
    private val onBiometricAuthSuccess = MutableLiveData<BiometricPrompt.PromptInfo>()
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

    fun onEmailTextChanged(email: Editable) {
        currentEmail.value = email.toString().trim { it <= ' ' }
    }

    fun onPasswordTextChanged(password: Editable) {
        currentPassword.value = password.toString().trim { it <= ' ' }
    }

    fun onError(): MutableLiveData<String> {
        return onError
    }

    fun onBiometricAuthSuccess(): MutableLiveData<BiometricPrompt.PromptInfo> {
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
            onError.value = "Please enter a valid email address"
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
                onError.value = error
            }
        })
    }

    fun useTouchId() {
        val biometricManager = BiometricManager.from(getApplication())

        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS ->
            onBiometricAuthSuccess.value = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Use account password")
                .build()
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                onError.value = "No biometric features available on this device."
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                onError.value = "Biometric features are currently unavailable."
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                onError.value = "You haven't associated " +
                        "any biometric credentials with your account."
        }
    }

    fun login() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        val email = currentEmail.value
        val password = currentPassword.value

        if (!TextUtils.isEmpty(email)) {
            val pattern: Pattern = Patterns.EMAIL_ADDRESS
            if (!pattern.matcher(email).matches()) {
                onError.value = "Email"
                return
            }

        } else {
            onError.value = "Please enter a valid email address"
            return
        }

        if (TextUtils.isEmpty(password)) {
            onError.value = "Please enter your password"
            return
        }

        isBusy.value = true
        loginRepository?.login(email, password, object : ILoginRepository.IOnLoginListener {
            override fun onSuccess() {
                isBusy.value = false
                onLoggedIn.call()
            }

            override fun onFailed(error: String?, errorCode: Int) {
                isBusy.value = false
                onError.value = error
            }
        })
    }
}