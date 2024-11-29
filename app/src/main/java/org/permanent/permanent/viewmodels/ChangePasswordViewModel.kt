package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.AccountRepositoryImpl
import org.permanent.permanent.repositories.IAccountRepository

class ChangePasswordViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private var appContext = application.applicationContext

    private val isBusy = MutableLiveData<Boolean>()
    private val showMessage = MutableLiveData<String>()
    private val onPasswordChanged = SingleLiveEvent<Void?>()
    private val currentPassword = MutableLiveData<String>()
    private val newPassword = MutableLiveData<String>()
    private val retypeNewPassword = MutableLiveData<String>()
    private var accountRepository: IAccountRepository = AccountRepositoryImpl(application)

    fun onCurrentPasswordTextChanged(password: Editable) {
        currentPassword.value = password.toString().trim { it <= ' ' }
    }

    fun onNewPasswordTextChanged(password: Editable) {
        newPassword.value = password.toString().trim { it <= ' ' }
    }

    fun onRetypeNewPasswordTextChanged(password: Editable) {
        retypeNewPassword.value = password.toString().trim { it <= ' ' }
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

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getShowMessage(): LiveData<String> = showMessage

    fun getOnPasswordChanged(): LiveData<Void?> = onPasswordChanged

    fun getCurrentPassword(): MutableLiveData<String> = currentPassword

    fun getNewPassword(): MutableLiveData<String> = newPassword

    fun getRetypeNewPassword(): MutableLiveData<String> = retypeNewPassword
}