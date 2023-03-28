package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.Validator
import org.permanent.permanent.repositories.AuthenticationRepositoryImpl
import org.permanent.permanent.repositories.IAuthenticationRepository

class ResetPasswordViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val newPassword = MutableLiveData<String>()
    private val passwordConfirmation = MutableLiveData<String>()
    private val newPasswordError = MutableLiveData<Int>()
    private val passwordConfirmationError = MutableLiveData<Int>()
    private val errorMessage = MutableLiveData<String>()
    private val onPasswordReset = SingleLiveEvent<Void>()
    private val onBackToSignIn = SingleLiveEvent<Void>()
    private val isBusy = MutableLiveData<Boolean>()
    private var authRepository: IAuthenticationRepository =
        AuthenticationRepositoryImpl(application)

    fun onResetPasswordBtnClick() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        newPassword.value = newPassword.value?.trim()
        passwordConfirmation.value = passwordConfirmation.value?.trim()

        val password = newPassword.value
        val passwordConfirmation = passwordConfirmation.value

        if (!Validator.isValidPassword(password, newPasswordError)) return
        if (!Validator.doPasswordsMatch(
                password, passwordConfirmation, passwordConfirmationError
            )
        ) return

        isBusy.value = true
        authRepository.resetPassword(password!!, passwordConfirmation!!,
            object : IAuthenticationRepository.IOnResetPasswordListener {
                override fun onSuccess() {
                    isBusy.value = false
                    onPasswordReset.call()
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    error?.let { errorMessage.value = it }
                }
            })
    }

    fun onBackToSignInBtnClick() {
        onBackToSignIn.call()
    }

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getNewPassword(): MutableLiveData<String> = newPassword

    fun getPasswordConfirmation(): MutableLiveData<String> = passwordConfirmation

    fun getErrorMessage(): LiveData<String> = errorMessage

    fun getNewPasswordError(): LiveData<Int> = newPasswordError

    fun getPasswordConfirmationError(): LiveData<Int> = passwordConfirmationError

    fun getOnPasswordReset(): MutableLiveData<Void> = onPasswordReset

    fun getOnBackToSignIn(): MutableLiveData<Void> = onBackToSignIn

    fun onNewPasswordTextChanged(password: Editable) {
        newPassword.value = password.toString().trim { it <= ' ' }
    }

    fun onPasswordConfirmationTextChanged(password: Editable) {
        passwordConfirmation.value = password.toString().trim { it <= ' ' }
    }
}
