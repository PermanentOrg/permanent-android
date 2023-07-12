package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData

class ArchivesContainerViewModel(application: Application) :
    ObservableAndroidViewModel(application) {

    private val onDismissRequest = SingleLiveEvent<Void?>()

    fun onDoneBtnClick() {
        onDismissRequest.call()
    }

    fun getOnDismissRequest(): MutableLiveData<Void?> = onDismissRequest
}
