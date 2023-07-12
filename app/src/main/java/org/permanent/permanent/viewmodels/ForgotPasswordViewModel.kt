package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.Validator
import org.permanent.permanent.repositories.AuthenticationRepositoryImpl
import org.permanent.permanent.repositories.IAuthenticationRepository

class ForgotPasswordViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val currentEmail = MutableLiveData<String>()
    private val emailError = MutableLiveData<Int>()
    private val errorMessage = MutableLiveData<String>()
    private val onPasswordReset = SingleLiveEvent<Void?>()
    private val onBackToSignIn = SingleLiveEvent<String>()
    private val isBusy = MutableLiveData<Boolean>()
    private var authRepository: IAuthenticationRepository =
        AuthenticationRepositoryImpl(application)

    fun onRecoverPasswordBtnClick() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        currentEmail.value = currentEmail.value?.trim()

        val email = currentEmail.value

        if (!Validator.isValidEmail(null, email, emailError, null)) return

        isBusy.value = true
        authRepository.forgotPassword(email!!,
            object : IAuthenticationRepository.IOnResetPasswordListener {
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

    fun onBackToSignInBtnClick() {
        onBackToSignIn.call()
    }

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getCurrentEmail(): MutableLiveData<String> = currentEmail

    fun getErrorMessage(): LiveData<String> = errorMessage

    fun getEmailError(): LiveData<Int> = emailError

    fun getOnPasswordReset(): MutableLiveData<Void?> = onPasswordReset

    fun getOnBackToSignIn(): MutableLiveData<String> = onBackToSignIn

    fun onEmailTextChanged(email: Editable) {
        currentEmail.value = email.toString().trim { it <= ' ' }
    }
}
