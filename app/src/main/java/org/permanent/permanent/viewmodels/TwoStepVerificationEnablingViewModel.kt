package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R

import java.util.regex.Pattern


class TwoStepVerificationEnablingViewModel(application: Application) :
    ObservableAndroidViewModel(application) {
    private val currentPhoneNumber = MutableLiveData<String>()
    private val onSkipTwoStep = SingleLiveEvent<Void>()
    private val onSubmit = SingleLiveEvent<Void>()
    private val phoneError = MutableLiveData<Int>()

    fun onCurrentPhoneNumberChanged(number: Editable) {
        currentPhoneNumber.value = number.toString().trim { it <= ' ' }
    }

    fun getCurrentPhoneNumber(): MutableLiveData<String> {
        return currentPhoneNumber
    }

    fun onSubmit(): LiveData<Void> {
        return onSubmit
    }

    fun onSkipTwoStep(): LiveData<Void> {
        return onSkipTwoStep
    }

    fun skipTwoStep() {
        onSkipTwoStep.call()
    }

    fun getPhoneError(): LiveData<Int> {
        return phoneError
    }

    private fun checkPhoneNumber(number: String?): Boolean {
        if (number == null) {
            return false
        }
        return Pattern.matches("^[+]?[0-9]{8,13}\$", number)
    }

    fun submit() {
        val phoneNumber = currentPhoneNumber.value
        if (!checkPhoneNumber(phoneNumber)) {
            phoneError.value = R.string.two_step_verification_phone_error_message
            return
        } else {
            phoneError.value = null
        }

        onSubmit.call()


    }
}