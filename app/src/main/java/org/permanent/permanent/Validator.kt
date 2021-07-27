package org.permanent.permanent

import androidx.core.util.PatternsCompat
import androidx.lifecycle.MutableLiveData

class Validator {

    companion object {
        private const val MIN_PASSWORD_LENGTH = 8

        fun isValidName(name: String?, nameError: MutableLiveData<Int>): Boolean {
            return if (name.isNullOrEmpty()) {
                nameError.value = R.string.invalid_name_error
                false
            } else {
                nameError.value = null
                true
            }
        }

        fun isValidEmail(email: String?, emailError: MutableLiveData<Int>): Boolean {
            return if (email.isNullOrEmpty()
                || !PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()
            ) {
                emailError.value = R.string.invalid_email_error
                false
            } else {
                emailError.value = null
                true
            }
        }

        fun isValidPassword(password: String?, passwordError: MutableLiveData<Int>): Boolean {
            return when {
                password.isNullOrEmpty() -> {
                    passwordError.value = R.string.password_empty_error
                    false
                }
                password.length < MIN_PASSWORD_LENGTH -> {
                    passwordError.value = R.string.password_too_short_error
                    false
                }
                else -> {
                    passwordError.value = null
                    true
                }
            }
        }
    }
}