package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.network.models.AccountVO
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
    private val isBusy = MutableLiveData(false)
    private val showMessage = MutableLiveData<String>()
    private val showSnackbar = MutableLiveData<Int>()
    private val onShowAddDialogRequest = MutableLiveData<Void>()
    private var fileRepository: IFileRepository = FileRepositoryImpl(appContext)

    init {
        getMembers()
    }

    private fun getMembers() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        fileRepository.getMembers(object : IFileRepository.IOnMembersListener {

            override fun onSuccess(members: List<AccountVO>?) {
                isBusy.value = false
//                if (!members.isNullOrEmpty()) onMembersRetrieved.value = members
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showMessage.value = error
            }
        })
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

    fun getShowMessage(): LiveData<String> {
        return showMessage
    }

    fun getShowAddDialogRequest(): LiveData<Void> {
        return onShowAddDialogRequest
    }

    fun getShowSnackbar(): LiveData<Int> {
        return showSnackbar
    }

    fun onInfoBtnClick(stringInt: Int) {
        showSnackbar.value = stringInt
    }

    fun onAddFabClick() {
        onShowAddDialogRequest.value = onShowAddDialogRequest.value
    }
}