package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData

abstract class AddEditLegacyEntityViewModel(application: Application) :
    ObservableAndroidViewModel(application) {
    val showError = MutableLiveData<String>()
    var name: String? = null
    var email: String? = null
    var message: String? = null

    abstract fun onSaveBtnClick(email: String, name: String?, message: String?)
}