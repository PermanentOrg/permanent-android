package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.Editable
import androidx.lifecycle.MutableLiveData

class CodeVerificationViewModel(application: Application): ObservableAndroidViewModel(application) {

    private val currentVerificationCode = MutableLiveData<String>()

    fun getCurrentVerificationCode() : MutableLiveData<String>{
        return currentVerificationCode
    }

    fun onCurrentVerificationCodeChanged(currentCode : Editable) {

    }

}