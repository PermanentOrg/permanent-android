package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData

class GiftStorageViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    val showError = MutableLiveData<String>()
    private val isBusy = MutableLiveData(false)
    private var spaceTotal = MutableLiveData(0L)
    private var spaceLeft = MutableLiveData(0L)
    private var spaceUsedPercentage = MutableLiveData(0)

    fun setSpaceTotal(it: Long) {
        spaceTotal.value = it
    }

    fun setSpaceLeft(it: Long) {
        spaceLeft.value = it
    }

    fun setSpaceUsedPercentage(it: Int) {
        spaceUsedPercentage.value = it
    }

    fun getSpaceTotal() = spaceTotal

    fun getSpaceLeft() = spaceLeft

    fun getSpaceUsedPercentage() = spaceUsedPercentage
}
