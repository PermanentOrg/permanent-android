package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.models.Account
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.AccountRepositoryImpl
import org.permanent.permanent.repositories.AuthenticationRepositoryImpl
import org.permanent.permanent.repositories.IAccountRepository
import org.permanent.permanent.repositories.IAuthenticationRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import java.util.regex.Pattern


class PhoneVerificationViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))
    private val currentPhoneNumber = MutableLiveData<String>()
    private val onVerificationSkipped = SingleLiveEvent<Void>()
    private val onSMSCodeSent = SingleLiveEvent<Void>()
    private val phoneError = MutableLiveData<Int>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onErrorMessage = MutableLiveData<String>()
    private var accountRepository: IAccountRepository = AccountRepositoryImpl(application)
    private var authRepository: IAuthenticationRepository = AuthenticationRepositoryImpl(application)

    fun onCurrentPhoneNumberChanged(number: Editable) {
        currentPhoneNumber.value = number.toString().trim { it <= ' ' }
    }

    fun getCurrentPhoneNumber(): MutableLiveData<String> {
        return currentPhoneNumber
    }

    fun getPhoneError(): LiveData<Int> {
        return phoneError
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getOnVerificationSkipped(): MutableLiveData<Void> {
        return onVerificationSkipped
    }

    fun getOnSMSCodeSent(): MutableLiveData<Void> {
        return onSMSCodeSent
    }

    fun getOnErrorMessage(): LiveData<String> {
        return onErrorMessage
    }

    fun skipTwoStep() {
        onVerificationSkipped.call()
        prefsHelper.saveSkipTwoStepVerification(true)
    }

    fun submit() {
        val phoneNumber = currentPhoneNumber.value
        if (!checkPhoneNumber(phoneNumber)) return
        phoneNumber?.let { updateAccount(it) }
    }

    private fun checkPhoneNumber(number: String?): Boolean {
        return if (number.isNullOrEmpty() || !Pattern.matches("^[+]?[0-9]{8,13}\$", number)) {
            phoneError.value = R.string.invalid_phone_error
            false
        } else {
            phoneError.value = null
            true
        }
    }

    private fun updateAccount(phone: String) {
        val accountId = prefsHelper.getAccountId()
        val email = prefsHelper.getAccountEmail()
        val account = Account(accountId, email)
        account.phone = phone

        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        isBusy.value = true
        accountRepository.update(account, object : IResponseListener {

            override fun onSuccess(message: String?) {
                isBusy.value = false
                sendSMSVerificationCode()
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                onErrorMessage.value = error
            }
        })
    }

    private fun sendSMSVerificationCode() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        isBusy.value = true
        authRepository.sendSMSVerificationCode(
            object : IAuthenticationRepository.IOnSMSCodeSentListener {
                override fun onSuccess() {
                    isBusy.value = false
                    onSMSCodeSent.call()
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    onErrorMessage.value = error
                }
            })
    }
}