package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.Editable
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import java.util.regex.Pattern

class ForgotPasswordViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val currentEmail = MutableLiveData<String>()
    private val emailError = MutableLiveData<Int>()

    fun getCurrentEmail(): MutableLiveData<String> {
        return currentEmail
    }

    fun getEmailError(): LiveData<Int> {
        return emailError
    }

    fun onEmailTextChanged(email: Editable) {
        currentEmail.value = email.toString().trim { it <= ' ' }
    }

    fun getValidatedEmail(): String? {
        val pattern: Pattern = Patterns.EMAIL_ADDRESS
        val email = currentEmail.value
        if (email.isNullOrEmpty() || !pattern.matcher(email).matches()) {
            emailError.value = R.string.invalid_email_error
            return null
        }
        emailError.value = null
        return email
    }
}
