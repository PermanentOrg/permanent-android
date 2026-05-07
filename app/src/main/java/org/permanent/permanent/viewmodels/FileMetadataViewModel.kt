package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.network.models.FileData

class FileMetadataViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val thumbURL = MutableLiveData<String>()
    private val showMessage = MutableLiveData<String>()
    val isBusy = MutableLiveData(false)

    fun setFileData(fileData: FileData) {
        thumbURL.value = fileData.thumbnail256 ?: fileData.thumbURL2000 ?: ""
    }

    fun getFileThumbURL(): MutableLiveData<String> = thumbURL

    fun getShowMessage(): LiveData<String> = showMessage
}