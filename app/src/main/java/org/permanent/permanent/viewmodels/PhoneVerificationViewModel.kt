package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.repositories.AccountRepositoryImpl
import org.permanent.permanent.repositories.IAccountRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import java.util.regex.Pattern


class PhoneVerificationViewModel(application: Application) :
    ObservableAndroidViewModel(application) {
    private val currentPhoneNumber = MutableLiveData<String>()
    private val onVerificationReady = SingleLiveEvent<Void>()
    private val phoneError = MutableLiveData<Int>()
    private val isBusy = MutableLiveData<Boolean>()
    val onErrorMessage = MutableLiveData<String>()
    private var accountRepository: IAccountRepository = AccountRepositoryImpl(application)
    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val prefsHelper = PreferencesHelper(sharedPreferences)

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

    fun getOnVerificationReady(): MutableLiveData<Void> {
        return onVerificationReady
    }

    fun getOnErrorMessage(): LiveData<String> {
        return onErrorMessage
    }

    fun skipTwoStep() {
        onVerificationReady.call()
    }

    fun submit() {
        val phoneNumber = currentPhoneNumber.value
        if (!checkPhoneNumber(phoneNumber)) return
        phoneNumber?.let { updateAccount(it) }
    }

    private fun checkPhoneNumber(number: String?): Boolean {
        return if (number.isNullOrEmpty() || !Pattern.matches("^[+]?[0-9]{8,13}\$", number)) {
            phoneError.value = R.string.two_step_verification_phone_error_message
            false
        } else {
            phoneError.value = null
            true
        }
    }

    private fun updateAccount(phone: String) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        isBusy.value = true
        accountRepository.update(phone, object : IAccountRepository.IOnPhoneUpdatedListener {
            override fun onSuccess() {
                isBusy.value = false
                prefsHelper.savePhoneVerified()
                onVerificationReady.call()
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                onErrorMessage.value = error
            }
        })
    }
}