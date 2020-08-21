package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.Editable
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.repositories.ISignUpRepository

class SignUpViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val onError = MutableLiveData<String>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onSignedUp = SingleLiveEvent<Void>()
    private val onAlreadyHaveAccount = SingleLiveEvent<Void>()

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

    fun onNameTextChanged(name: Editable) {
        currentName.value = name.toString()
    }

    fun onEmailTextChanged(email: Editable) {
        currentEmail.value = email.toString().trim { it <= ' ' }
    }

    fun onPasswordTextChanged(password: Editable) {
        currentPassword.value = password.toString().trim { it <= ' ' }
    }

    fun onError(): MutableLiveData<String> {
        return onError
    }

    fun onIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun onSignedUp(): MutableLiveData<Void> {
        return onSignedUp
    }

    fun onAlreadyHaveAccount(): MutableLiveData<Void> {
        return onAlreadyHaveAccount
    }

    fun alreadyHaveAccount() {
        onAlreadyHaveAccount.call()
    }

    fun signUp() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        val name = currentName.value?.trim { it <= ' ' }
        val email = currentEmail.value
        val password = currentPassword.value

        if (TextUtils.isEmpty(name)) {
            onError.value = "Please enter your full name"
            return
        }

        if (TextUtils.isEmpty(email)) {
            onError.value = "Please enter a valid email address"
            return
        }

        if (TextUtils.isEmpty(password)) {
            onError.value = "Please enter your password"
            return
        }

        isBusy.value = true
        signUpRepository?.signUp(name, email, password, object : ISignUpRepository.IOnSignUpListener {
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
