package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData

class DeepLinkViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val isBusy = MutableLiveData(false)

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun onRequestAccessBtnClick() {
    }
}