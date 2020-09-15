package org.permanent.permanent.ui.twoStepVerification

import android.app.Application
import android.text.Editable
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.viewmodels.ObservableAndroidViewModel

class TwoStepVerificationCodeViewModel(application: Application): ObservableAndroidViewModel(application) {

    private val currentVerificationCode = MutableLiveData<String>()

    fun getCurrentVerificationCode() : MutableLiveData<String>{
        return currentVerificationCode
    }

    fun onCurrentVerificationCodeChanged(currentCode : Editable) {

    }

}