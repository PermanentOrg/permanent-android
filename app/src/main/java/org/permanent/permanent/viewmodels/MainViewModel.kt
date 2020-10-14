package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData

class MainViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val currentAccount = MutableLiveData<String>()
    private val currentSpaceUsed = MutableLiveData<Int>()

    fun getCurrentAccount(): MutableLiveData<String> {
        return currentAccount
    }

    fun getCurrentSpaceUsed(): MutableLiveData<Int> {
        return currentSpaceUsed
    }
}