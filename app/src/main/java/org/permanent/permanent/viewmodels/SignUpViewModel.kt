package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.Validator
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.AccountRepositoryImpl
import org.permanent.permanent.repositories.IAccountRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.showLoginScreen


class SignUpViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private var prefsHelper: PreferencesHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val nameError = MutableLiveData<Int>()
    private val emailError = MutableLiveData<Int>()
    private val passwordError = MutableLiveData<Int>()
    private val onErrorMessage = MutableLiveData<String>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onLoggedIn = SingleLiveEvent<Void>()
    private val onReadyToShowTermsDialog = SingleLiveEvent<Void>()
    private val currentName = MutableLiveData<String>()
    private val currentEmail = MutableLiveData<String>()
    private val currentPassword = MutableLiveData<String>()
    private var accountRepository: IAccountRepository = AccountRepositoryImpl(application)

    fun getCurrentName(): MutableLiveData<String> = currentName

    fun getCurrentEmail(): MutableLiveData<String> = currentEmail

    fun getCurrentPassword(): MutableLiveData<String> = currentPassword

    fun getNameError(): LiveData<Int> = nameError

    fun getEmailError(): LiveData<Int> = emailError

    fun getPasswordError(): LiveData<Int> = passwordError

    fun getOnErrorMessage(): MutableLiveData<String> = onErrorMessage

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getOnLoggedIn(): MutableLiveData<Void> = onLoggedIn

    fun getOnReadyToShowTermsDialog(): MutableLiveData<Void> = onReadyToShowTermsDialog

    fun onNameTextChanged(name: Editable) {
        currentName.value = name.toString()
    }

    fun onEmailTextChanged(email: Editable) {
        currentEmail.value = email.toString()
    }

    fun onPasswordTextChanged(password: Editable) {
        currentPassword.value = password.toString()
    }

    fun alreadyHaveAccount() {
        appContext.showLoginScreen()
    }

    fun onSignUpBtnClick() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        currentName.value = currentName.value?.trim()
        currentEmail.value = currentEmail.value?.trim()
        currentPassword.value = currentPassword.value?.trim()

        val name = currentName.value
        val email = currentEmail.value
        val password = currentPassword.value

        if (!Validator.isValidName(null, name, nameError, null)) return
        if (!Validator.isValidEmail(null, email, emailError, null)) return
        if (!Validator.isValidPassword(password, passwordError)) return

        onReadyToShowTermsDialog.call()
    }

    fun makeAccount() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        val name = currentName.value
        val email = currentEmail.value
        val password = currentPassword.value

        if (name != null && email != null && password != null) {
            isBusy.value = true
            accountRepository.signUp(name, email, password, object : IResponseListener {

                override fun onSuccess(message: String?) {
                    isBusy.value = false
                    prefsHelper.saveSkipTwoStepVerification(false)
                    appContext.showLoginScreen()
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    error?.let { onErrorMessage.value = it }
                }
            })
        }
    }
}
