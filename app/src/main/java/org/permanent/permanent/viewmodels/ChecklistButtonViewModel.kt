package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Account
import org.permanent.permanent.repositories.AccountRepositoryImpl
import org.permanent.permanent.repositories.IAccountRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

abstract class ChecklistButtonViewModel(application: Application) :
    ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val showChecklistFab = MutableLiveData(false)
    protected val showMessage = SingleLiveEvent<String>()
    protected var accountRepository: IAccountRepository = AccountRepositoryImpl(application)

    fun getHideChecklist() {
        accountRepository.getAccount(object : IAccountRepository.IAccountListener {

            override fun onSuccess(account: Account) {
                showChecklistFab.value = account.hideChecklist != null && !account.hideChecklist!!
                prefsHelper.saveAccountHideChecklist(account.hideChecklist != null && account.hideChecklist!!)
            }

            override fun onFailed(error: String?) {
                error?.let { showMessage.value = it }
            }
        })
    }

    fun hideChecklistButton() {
        showChecklistFab.value = false
    }

    fun getShowChecklistFab(): MutableLiveData<Boolean> = showChecklistFab
}