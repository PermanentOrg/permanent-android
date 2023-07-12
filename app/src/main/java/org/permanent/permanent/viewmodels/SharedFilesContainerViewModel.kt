package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData

class SharedFilesContainerViewModel(application: Application) :
    ObservableAndroidViewModel(application) {

    private val onSaveFolderRequest = SingleLiveEvent<Void?>()
    private val onCancelRequest = SingleLiveEvent<Void?>()

    fun onCancelBtnClick() {
        onCancelRequest.call()
    }

    fun onSaveBtnClick() {
        onSaveFolderRequest.call()
    }

    fun getOnSaveFolderRequest(): MutableLiveData<Void?> = onSaveFolderRequest

    fun getOnCancelRequest(): MutableLiveData<Void?> = onCancelRequest
}
