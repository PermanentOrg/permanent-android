package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.repositories.AccountRepositoryImpl
import org.permanent.permanent.repositories.AuthenticationRepositoryImpl
import org.permanent.permanent.repositories.IAccountRepository
import org.permanent.permanent.repositories.IAuthenticationRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import java.util.regex.Pattern


class PhoneVerificationViewModel(application: Application) :
    ObservableAndroidViewModel(application) {
    private val currentPhoneNumber = MutableLiveData<String>()
    private val onLoggedIn = SingleLiveEvent<Void>()
    private val phoneError = MutableLiveData<Int>()
    private val isBusy = MutableLiveData<Boolean>()
    val onErrorMessage = MutableLiveData<String>()
    private var accountRepository: IAccountRepository = AccountRepositoryImpl(application)
    private var authRepository: IAuthenticationRepository = AuthenticationRepositoryImpl(application)
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

    fun getOnLoggedIn(): MutableLiveData<Void> {
        return onLoggedIn
    }

    fun getOnErrorMessage(): LiveData<String> {
        return onErrorMessage
    }

    fun skipTwoStep() {
        val email = prefsHelper.getEmail()
        val password = prefsHelper.getPassword()

        if (email != null && password != null) {
            login(email, password)
        }
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
        val email = prefsHelper.getEmail()
        val password = prefsHelper.getPassword()

        if (email != null && password != null) {
            isBusy.value = true
            accountRepository.updatePhoneNumber(
                phone,
                object : IAccountRepository.IOnPhoneUpdatedListener {
                    override fun onSuccess() {
                        isBusy.value = false
                        login(email, password)
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        onErrorMessage.value = error
                    }
                })
        }
    }

    fun login(email: String, password: String) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        isBusy.value = true
        authRepository.login(email, password, object : IAuthenticationRepository.IOnLoginListener {
            override fun onSuccess() {
                isBusy.value = false
                onLoggedIn.call()
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                onErrorMessage.value = error
            }
        })
    }
}