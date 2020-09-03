package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MainActivityViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val currentAccount = MutableLiveData<String>()
    private val currentSpaceUsed = MutableLiveData<Int>()

    fun currentAccount():MutableLiveData<String>{
        return currentAccount
    }

    fun currentSpaceUsed():MutableLiveData<Int>{
        return currentSpaceUsed
    }

}