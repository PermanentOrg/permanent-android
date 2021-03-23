package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData

class LocationSearchViewModel(application: Application) : ObservableAndroidViewModel(application) {

    val showMessage = MutableLiveData<String>()
    val isBusy = MutableLiveData(false)
}