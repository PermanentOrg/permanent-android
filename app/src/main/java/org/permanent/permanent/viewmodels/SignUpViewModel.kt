package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Event
import org.permanent.permanent.repositories.ISignUpRepository
import java.util.regex.Pattern
import kotlin.math.log


class SignUpViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val onError = MutableLiveData<String>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onSignedUp = SingleLiveEvent<Void>()
    private val onAlreadyHaveAccount = SingleLiveEvent<Void>()

    private val currentName = MutableLiveData<String>()
    private val currentEmail = MutableLiveData<String>()
    private val currentPassword = MutableLiveData<String>()
    private var signUpRepository: ISignUpRepository? = null

    val displayTermsOfServiceTextDialog = MutableLiveData<Event<Unit>>()

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
        currentName.value = name.toString().trim { it <= ' ' }
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
        val name = currentName.value
        val email = currentEmail.value
        val password = currentPassword.value

        if (TextUtils.isEmpty(name)) {
            onError.value = "Name"
            return
        }

        if (!TextUtils.isEmpty(email)) {
            val pattern: Pattern = Patterns.EMAIL_ADDRESS
            if (!pattern.matcher(email).matches()) {
                onError.value = "Email"
                return
            }

        } else {
            onError.value = "Email"
            return
        }

        if (TextUtils.isEmpty(password)) {
            onError.value = "Password"
            return
        }

        displayTermsOfServiceTextDialog.value = Event(Unit)
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
