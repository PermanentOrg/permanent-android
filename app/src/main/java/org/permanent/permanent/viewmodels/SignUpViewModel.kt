package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.Editable
import android.text.TextUtils
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.repositories.ISignUpRepository
import java.util.regex.Pattern


class SignUpViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val nameError = MutableLiveData<Int>()
    private val emailError = MutableLiveData<Int>()
    private val passwordError = MutableLiveData<Int>()
    private val onError = MutableLiveData<String>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onSignedUp = SingleLiveEvent<Void>()
    private val onAlreadyHaveAccount = SingleLiveEvent<Void>()
    private val displayTermsOfServiceTextDialog = SingleLiveEvent<Void>()

    private val currentName = MutableLiveData<String>()
    private val currentEmail = MutableLiveData<String>()
    private val currentPassword = MutableLiveData<String>()
    private var signUpRepository: ISignUpRepository? = null


    init {
        //TODO implement signUp repository
//        signUpRepository = FirebaseSignUpRepository()
    }

    fun getCurrentName(): MutableLiveData<String>? {
        return currentName
    }

    fun getCurrentEmail(): MutableLiveData<String>? {
        return currentEmail
    }

    fun getCurrentPassword(): MutableLiveData<String>? {
        return currentPassword
    }

    fun getNameError(): LiveData<Int> {
        return nameError
    }

    fun getEmailError(): LiveData<Int> {
        return emailError
    }

    fun getPasswordError(): LiveData<Int> {
        return passwordError
    }

    fun onNameTextChanged(name: Editable) {
        currentName.value = name.toString().trim { it <= ' ' }
    }

    fun onEmailTextChanged(email: Editable) {
        currentEmail.value = email.toString().trim { it <= ' ' }
    }

    fun onPasswordTextChanged(password: Editable) {
        currentPassword.value = password.toString().trim { it <= ' ' }
    }

    fun getOnError(): MutableLiveData<String> {
        return onError
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getOnSignedUp(): MutableLiveData<Void> {
        return onSignedUp
    }

    fun getOnAlreadyHaveAccount(): MutableLiveData<Void> {
        return onAlreadyHaveAccount
    }

    fun alreadyHaveAccount() {
        onAlreadyHaveAccount.call()
    }

    fun getDisplayTermsOfServiceTextDialog(): MutableLiveData<Void> {
        return displayTermsOfServiceTextDialog
    }

    private fun checkName(name: String?): Boolean {
        return if (TextUtils.isEmpty(name)) {
            nameError.value = R.string.sign_up_empty_name_error
            false
        } else {
            nameError.value = null
            true
        }
    }

    private fun checkEmail(email: String?): Boolean {
        if (email.isNullOrEmpty()) {
            emailError.value = R.string.invalid_email_error
            return false
        } else {
            val pattern: Pattern = Patterns.EMAIL_ADDRESS
            if (!pattern.matcher(email).matches()) {
                emailError.value = R.string.invalid_email_error
                return false
            }

        }
        emailError.value = null
        return true
    }

    private fun checkEmptyPassword(password: String?): Boolean {
        if (password == null) {
            passwordError.value = R.string.no_password_error
            return false
        } else {
            if (password.length < Constants.MIN_PASSWORD_LENGTH) {
                passwordError.value = R.string.sign_up_password_too_small_error
                return false
            }
        }
        passwordError.value = null
        return true
    }

    fun signUp() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        val name = currentName.value
        val email = currentEmail.value
        val password = currentPassword.value

        if (!checkName(name)) return

        if (!checkEmail(email)) return

        if (!checkEmptyPassword(password)) return

        displayTermsOfServiceTextDialog.call()
    }


    fun makeAccount() {
        val name = currentName.value
        val email = currentEmail.value
        val password = currentPassword.value
        isBusy.value = true
        signUpRepository?.signUp(
            name,
            email,
            password,
            object : ISignUpRepository.IOnSignUpListener {
                override fun onSuccess() {
                    isBusy.value = false
                    onSignedUp.call()
                }

                override fun onFailed(error: String?, errorCode: Int) {
                    isBusy.value = false
                    onError.value = error
                }
            })
    }
}
