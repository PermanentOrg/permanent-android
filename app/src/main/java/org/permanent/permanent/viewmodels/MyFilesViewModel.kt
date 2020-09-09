package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData

class MyFilesViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val existsFiles = MutableLiveData(true)

    fun getExistsFiles(): MutableLiveData<Boolean> {
        return existsFiles
    }
}