package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.PermanentApplication
import org.permanent.permanent.R
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.AccountRepositoryImpl
import org.permanent.permanent.repositories.IAccountRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class DeleteAccountViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val prefsHelper = PreferencesHelper(
        PermanentApplication.instance.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))

    private val isBusy = MutableLiveData<Boolean>()
    private val isDeleteAccountEnabled = MutableLiveData(false)
    private val onNavigateToLoginScreen = SingleLiveEvent<Void>()
    private val showMessage = MutableLiveData<String>()
    private val text = MutableLiveData<String>()
    private var accountRepository: IAccountRepository = AccountRepositoryImpl(application)

    fun getIsDeleteAccountEnabled(): MutableLiveData<Boolean> {
        return isDeleteAccountEnabled
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getOnNavigateToLoginScreen(): SingleLiveEvent<Void> {
        return onNavigateToLoginScreen
    }

    fun getShowMessage(): LiveData<String> {
        return showMessage
    }

    fun onTextChanged(inputText: Editable) {
        text.value = inputText.toString()
        isDeleteAccountEnabled.value = text.value?.trim() == "DELETE"
    }

    fun onDeleteAccountBtnClick() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        
        if(isDeleteAccountEnabled.value == false){
            showMessage.value = PermanentApplication.instance.getString(R.string.delete_account_error)
            return
        }

        isBusy.value = true
        accountRepository.delete(object : IResponseListener {
            override fun onSuccess(message: String?) {
                isBusy.value = false
                prefsHelper.saveUserLoggedIn(false)
                prefsHelper.saveBiometricsLogIn(true) // Setting back to default
                onNavigateToLoginScreen.call()
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showMessage.value = error
            }
        })
    }
}