package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.Editable
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R

class SecuritySettingsViewModel(application: Application) :
    ObservableAndroidViewModel(application) {
    private val isBusy = MutableLiveData<Boolean>()
    private val errorMessage = MutableLiveData<String>()

    private val currentPassword = MutableLiveData<String>()
    private val newPassword = MutableLiveData<String>()
    private val retypePassword = MutableLiveData<String>()


    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getErrorMessage(): LiveData<String> {
        return errorMessage
    }

    fun getCurrentPassword(): MutableLiveData<String>? {
        return currentPassword
    }

    fun getNewPassword(): MutableLiveData<String>? {
        return newPassword
    }

    fun getRetypePassword(): MutableLiveData<String>? {
        return retypePassword
    }

    fun onCurrentPasswordTextChanged(password: Editable) {
        currentPassword.value = password.toString().trim { it <= ' ' }
    }

    fun onNewPasswordTextChanged(password: Editable) {
        newPassword.value = password.toString().trim { it <= ' ' }
    }

    fun onRetypePasswordTextChanged(password: Editable) {
        retypePassword.value = password.toString().trim { it <= ' ' }
    }

    fun updatePassword() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        val currentPassword = currentPassword.value
        val newPassword = newPassword.value
        val retypedPassword = retypePassword.value

        if (TextUtils.isEmpty(currentPassword)) {
            errorMessage.value = "Please enter your current password"
            return
        }

        if (TextUtils.isEmpty(newPassword)) {
            errorMessage.value = "Please enter your new password"
            return
        }

        if (TextUtils.isEmpty(retypedPassword)) {
            errorMessage.value = "Please retype your new password"
            return
        }
        //TODO update password
    }
}