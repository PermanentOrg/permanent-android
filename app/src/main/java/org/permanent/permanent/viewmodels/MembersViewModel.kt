package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Account
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class MembersViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))
    private val ownerDisplayName = prefsHelper.getUserFullName()
    private val ownerEmail = prefsHelper.getEmail()
    private val existsManagers = MutableLiveData(false)
    private val existsCurators = MutableLiveData(false)
    private val existsEditors = MutableLiveData(false)
    private val existsContributors = MutableLiveData(false)
    private val existsViewers = MutableLiveData(false)
    private val onManagersRetrieved = MutableLiveData<List<Account>>()
    private val onCuratorsRetrieved = MutableLiveData<List<Account>>()
    private val onEditorsRetrieved = MutableLiveData<List<Account>>()
    private val onContributorsRetrieved = MutableLiveData<List<Account>>()
    private val onViewersRetrieved = MutableLiveData<List<Account>>()
    private val isBusy = MutableLiveData(false)
    private val showSnackbar = MutableLiveData<String>()
    private val showSnackbarLong = MutableLiveData<Int>()
    private val onShowAddDialogRequest = MutableLiveData<Void>()
    private var fileRepository: IFileRepository = FileRepositoryImpl(appContext)

    init {
        refreshMembers()
    }

    fun refreshMembers() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        fileRepository.getMembers(object : IFileRepository.IOnDataListener {

            override fun onSuccess(dataList: List<Datum>?) {
                isBusy.value = false
                if (!dataList.isNullOrEmpty()) splitByRole(dataList)
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showSnackbar.value = error
            }
        })
    }

    private fun splitByRole(allMembers: List<Datum>) {
        val managers: MutableList<Account> = ArrayList()
        val curators: MutableList<Account> = ArrayList()
        val editors: MutableList<Account> = ArrayList()
        val contributors: MutableList<Account> = ArrayList()
        val viewers: MutableList<Account> = ArrayList()
        val userAccountId = PreferencesHelper(
            appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        ).getUserAccountId()

        for (datum in allMembers) {
            val account = Account(datum.AccountVO)
            if (userAccountId != account.id) {
                when (account.accessRole) {
                    AccessRole.MANAGER -> managers.add(account)
                    AccessRole.CURATOR -> curators.add(account)
                    AccessRole.EDITOR -> editors.add(account)
                    AccessRole.CONTRIBUTOR -> contributors.add(account)
                    else -> viewers.add(account)
                }
            }
        }
        if (managers.isNotEmpty()) {
            onManagersRetrieved.value = managers
            existsManagers.value = true
        }
        if (curators.isNotEmpty()) {
            onCuratorsRetrieved.value = curators
            existsCurators.value = true
        }
        if (editors.isNotEmpty()) {
            onEditorsRetrieved.value = editors
            existsEditors.value = true
        }
        if (contributors.isNotEmpty()) {
            onContributorsRetrieved.value = contributors
            existsContributors.value = true
        }
        if (viewers.isNotEmpty()) {
            onViewersRetrieved.value = viewers
            existsViewers.value = true
        }
    }

    fun getOnManagersRetrieved(): LiveData<List<Account>> {
        return onManagersRetrieved
    }

    fun getOnCuratorsRetrieved(): LiveData<List<Account>> {
        return onCuratorsRetrieved
    }

    fun getOnEditorsRetrieved(): LiveData<List<Account>> {
        return onEditorsRetrieved
    }

    fun getOnContributorsRetrieved(): LiveData<List<Account>> {
        return onContributorsRetrieved
    }

    fun getOnViewersRetrieved(): LiveData<List<Account>> {
        return onViewersRetrieved
    }

    fun getOwnerDisplayName(): String? {
        return ownerDisplayName
    }

    fun getOwnerEmail(): String? {
        return ownerEmail
    }

    fun getExistsManagers(): MutableLiveData<Boolean> {
        return existsManagers
    }

    fun getExistsCurators(): MutableLiveData<Boolean> {
        return existsCurators
    }

    fun getExistsEditors(): MutableLiveData<Boolean> {
        return existsEditors
    }

    fun getExistsContributors(): MutableLiveData<Boolean> {
        return existsContributors
    }

    fun getExistsViewers(): MutableLiveData<Boolean> {
        return existsViewers
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getShowSnackbar(): LiveData<String> {
        return showSnackbar
    }

    fun getShowAddDialogRequest(): LiveData<Void> {
        return onShowAddDialogRequest
    }

    fun getShowSnackbarLong(): LiveData<Int> {
        return showSnackbarLong
    }

    fun onInfoBtnClick(stringInt: Int) {
        showSnackbarLong.value = stringInt
    }

    fun onAddFabClick() {
        onShowAddDialogRequest.value = onShowAddDialogRequest.value
    }
}