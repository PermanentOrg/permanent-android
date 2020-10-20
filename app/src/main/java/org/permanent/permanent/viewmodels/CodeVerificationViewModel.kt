package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.repositories.AuthenticationRepositoryImpl
import org.permanent.permanent.repositories.IAuthenticationRepository

class CodeVerificationViewModel(application: Application): ObservableAndroidViewModel(application) {

    var isSmsCodeFlow = false
    private val currentCode = MutableLiveData<String>()
    private val codeError = MutableLiveData<Int>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onCodeVerified = SingleLiveEvent<Void>()
    private val errorMessage = MutableLiveData<String>()
    private var authRepository: IAuthenticationRepository = AuthenticationRepositoryImpl(application)

    fun getVerificationCode() : MutableLiveData<String>{
        return currentCode
    }

    fun onCurrentCodeChanged(code : Editable) {
        currentCode.value = code.toString()
    }

    fun getCodeError(): LiveData<Int> {
        return codeError
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getOnCodeVerified(): MutableLiveData<Void> {
        return onCodeVerified
    }

    fun getErrorMessage(): LiveData<String> {
        return errorMessage
    }

    private fun isCodeValid(): Boolean {
        currentCode.value = currentCode.value?.trim()
        val trimmedCode = currentCode.value

        if (trimmedCode.isNullOrEmpty()) {
            codeError.value = R.string.verification_code_empty_error
            return false
        } else {
            if (trimmedCode.length < Constants.VERIFICATION_CODE_LENGTH
                || trimmedCode.length > Constants.VERIFICATION_CODE_LENGTH) {
                codeError.value = R.string.verification_code_length_error
                return false
            }
        }
        codeError.value = null
        return true
    }

    fun done() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        if (!isCodeValid()) return

        currentCode.value?.let {
            isBusy.value = true
            authRepository.verifyCode(it, getAuthType(),
                object : IAuthenticationRepository.IOnVerifyListener {
                override fun onSuccess() {
                    isBusy.value = false
                    onCodeVerified.call()
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    errorMessage.value = error
                }
            })
        }
    }

    private fun getAuthType(): String {
        if (isSmsCodeFlow) return Constants.AUTH_TYPE_PHONE
        return Constants.AUTH_TYPE_MFA_VALIDATION
    }
}