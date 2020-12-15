package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository

class MembersViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val existsOwners = MutableLiveData(true)
    private val existsCurators = MutableLiveData(false)
    private val existsEditors = MutableLiveData(false)
    private val existsContributors = MutableLiveData(false)
    private val existsViewers = MutableLiveData(false)
    private val isBusy = MutableLiveData(false)
    private val showMessage = MutableLiveData<String>()
    private val showSnackbar = MutableLiveData<Int>()
    private var fileRepository: IFileRepository = FileRepositoryImpl(appContext)

    fun getExistsOwners(): MutableLiveData<Boolean> {
        return existsOwners
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

    fun getShowSnackbar(): LiveData<Int> {
        return showSnackbar
    }

    fun onInfoBtnClick(stringInt: Int) {
        showSnackbar.value = stringInt
    }

    fun onAddFabClick() {
    }
}