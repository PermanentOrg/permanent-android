package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.network.models.FileData

class FileMetadataViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val thumbURL2000 = MutableLiveData<String>()
    private val showMessage = MutableLiveData<String>()
    val isBusy = MutableLiveData(false)

    fun setFileData(fileData: FileData) {
        fileData.thumbURL2000?.let { thumbURL2000.value = it }
    }

    fun getThumbURL2000(): MutableLiveData<String> {
        return thumbURL2000
    }

    fun getShowMessage(): LiveData<String> {
        return showMessage
    }
}