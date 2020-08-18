package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.repositories.ILoginRepository

class LoginViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val onError = MutableLiveData<String>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onLoggedIn = SingleLiveEvent<Void>()
    private val onRememberMe = SingleLiveEvent<Void>()

    private val currentEmail = MutableLiveData<String>()
    private val currentPassword = MutableLiveData<String>()
    private var loginRepository: ILoginRepository? = null

    init {
        //TODO implement login repository
//        loginRepository = FirebaseLoginRepository()
    }

    fun getCurrentEmail(): MutableLiveData<String>? {
        return currentEmail
    }

    fun getCurrentPassword(): MutableLiveData<String>? {
        return currentPassword
    }

    fun onEmailTextChanged(email: CharSequence) {
        currentEmail.value = email.toString().trim { it <= ' ' }
    }

    fun onPasswordTextChanged(password: CharSequence) {
        currentPassword.value = password.toString().trim { it <= ' ' }
    }

    fun onError(): MutableLiveData<String> {
        return onError
    }

    fun onIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun onLoggedIn(): MutableLiveData<Void> {
        return onLoggedIn
    }

    fun onRememberMe(): MutableLiveData<Void> {
        return onRememberMe
    }

    override fun onCleared() {
        super.onCleared()
    }

    fun rememberMe() {
        onRememberMe.call()
    }

    fun login() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        val email = currentEmail.value
        val password = currentPassword.value

        if (TextUtils.isEmpty(email)) {
            onError.value = "Please enter a valid email address"
            return
        }

        if (TextUtils.isEmpty(password)) {
            onError.value = "Please enter your password"
            return
        }

        isBusy.value = true
        loginRepository?.login(email, password, object : ILoginRepository.IOnLoginListener {
            override fun onSuccess() {
                isBusy.value = false
                onLoggedIn.call()
            }

            override fun onFailed(error: String?, errorCode: Int) {
                isBusy.value = false
                onError.value = error
            }
        })
    }
}