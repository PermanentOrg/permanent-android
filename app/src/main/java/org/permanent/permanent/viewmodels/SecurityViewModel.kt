package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.AccountRepositoryImpl
import org.permanent.permanent.repositories.IAccountRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class SecurityViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private var appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))

    private val isBusy = MutableLiveData<Boolean>()
    private val showMessage = MutableLiveData<String>()
    private val onPasswordChanged = SingleLiveEvent<Void>()
    private val currentPassword = MutableLiveData<String>()
    private val newPassword = MutableLiveData<String>()
    private val retypeNewPassword = MutableLiveData<String>()
    private val biometricsLogin = MutableLiveData(prefsHelper.isBiometricsLogIn())
    private var accountRepository: IAccountRepository = AccountRepositoryImpl(application)

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getShowMessage(): LiveData<String> {
        return showMessage
    }

    fun getOnPasswordChanged(): LiveData<Void> {
        return onPasswordChanged
    }

    fun getCurrentPassword(): MutableLiveData<String> {
        return currentPassword
    }

    fun getNewPassword(): MutableLiveData<String> {
        return newPassword
    }

    fun getRetypeNewPassword(): MutableLiveData<String> {
        return retypeNewPassword
    }

    fun getBiometricsLogin(): MutableLiveData<Boolean> {
        return biometricsLogin
    }

    fun onCurrentPasswordTextChanged(password: Editable) {
        currentPassword.value = password.toString().trim { it <= ' ' }
    }

    fun onNewPasswordTextChanged(password: Editable) {
        newPassword.value = password.toString().trim { it <= ' ' }
    }

    fun onRetypeNewPasswordTextChanged(password: Editable) {
        retypeNewPassword.value = password.toString().trim { it <= ' ' }
    }

    fun onBiometricsLoginChanged(checked: Boolean) {
        biometricsLogin.value = checked
        prefsHelper.saveBiometricsLogIn(checked)
    }

    fun onUpdatePasswordClick() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        val oldPass = currentPassword.value
        val newPass = newPassword.value
        val retypedPass = retypeNewPassword.value

        if (oldPass.isNullOrEmpty()) {
            showMessage.value = appContext.getString(R.string.invalid_current_password_error)
            return
        }

        if (newPass.isNullOrEmpty()) {
            showMessage.value = appContext.getString(R.string.invalid_new_password_error)
            return
        }

        if (retypedPass.isNullOrEmpty()) {
            showMessage.value = appContext.getString(R.string.invalid_retype_new_password_error)
            return
        }

        isBusy.value = true
        accountRepository.changePassword(oldPass, newPass, retypedPass,
            object : IResponseListener {
                override fun onSuccess(message: String?) {
                    isBusy.value = false
                    showMessage.value = message
                    onPasswordChanged.call()
                    currentPassword.value = ""
                    newPassword.value = ""
                    retypeNewPassword.value = ""
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    showMessage.value = error
                }
            }
        )
    }
}