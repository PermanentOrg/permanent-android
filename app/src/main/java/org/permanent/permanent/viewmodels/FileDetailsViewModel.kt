package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.network.models.FileData

class FileDetailsViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val filePath = MutableLiveData<String>()
    private val showMessage = MutableLiveData<String>()
    val isBusy = MutableLiveData(false)

    fun setFileData(fileData: FileData) {
    }

    fun getShowMessage(): LiveData<String> {
        return showMessage
    }
}