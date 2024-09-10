package org.permanent.permanent

import android.content.Context
import androidx.core.util.PatternsCompat
import androidx.lifecycle.MutableLiveData
import java.util.regex.Pattern

class Validator {

    companion object {
        private const val MIN_PASSWORD_LENGTH = 8

        fun isValidName(
            context: Context?,
            name: String?,
            intError: MutableLiveData<Int>?,
            stringError: MutableLiveData<String>?
        ): Boolean {
            return if (name.isNullOrEmpty()) {
                if (intError != null) intError.value = R.string.invalid_name_error
                else stringError?.value = context?.getString(R.string.invalid_name_error)
                false
            } else {
                intError?.value = null
                stringError?.value = null
                true
            }
        }

        fun isValidEmail(
            context: Context?,
            email: String?,
            intError: MutableLiveData<Int>?,
            stringError: MutableLiveData<String>?
        ): Boolean {
            return if (email.isNullOrEmpty()
                || !PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()
            ) {
                if (intError != null) intError.value = R.string.invalid_email_error
                else stringError?.value = context?.getString(R.string.invalid_email_error)
                false
            } else {
                intError?.value = null
                stringError?.value = null
                true
            }
        }

        fun isValidPassword(password: String?, passwordError: MutableLiveData<Int>?): Boolean {
            return when {
                password.isNullOrEmpty() -> {
                    passwordError?.value = R.string.password_empty_error
                    false
                }
                password.length < MIN_PASSWORD_LENGTH -> {
                    passwordError?.value = R.string.password_too_short_error
                    false
                }
                else -> {
                    passwordError?.value = null
                    true
                }
            }
        }

        fun isValidPhone(context: Context?, phone: String?, phoneError: MutableLiveData<String>): Boolean {
            return if (!phone.isNullOrEmpty()) {
                if(!Pattern.matches("^[+]?[0-9]{8,13}\$", phone)) {
                    phoneError.value = context?.getString(R.string.invalid_phone_error)
                    false
                } else {
                    phoneError.value = null
                    true
                }
            } else {
                phoneError.value = null
                true
            }
        }
    }
}