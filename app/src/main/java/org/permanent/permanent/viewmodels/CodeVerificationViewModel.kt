package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.repositories.ILoginRepository
import org.permanent.permanent.repositories.LoginRepositoryImpl

class CodeVerificationViewModel(application: Application): ObservableAndroidViewModel(application) {

    private val currentCode = MutableLiveData<String>()
    private val codeError = MutableLiveData<Int>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onCodeVerified = SingleLiveEvent<Void>()
    private var loginRepository: ILoginRepository = LoginRepositoryImpl(application)
    private val errorMessage = MutableLiveData<String>()

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

    private fun checkEmptyCode(code: String?): Boolean {
        if (code == null) {
            codeError.value = R.string.verification_code_empty_error
            return false
        } else {
            val trimmedCode = code.trim { it <= ' ' }
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
        val code = currentCode.value

        if (!checkEmptyCode(code)) return

        isBusy.value = true
        loginRepository.verify(code!!, object : ILoginRepository.IOnVerifyListener {
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