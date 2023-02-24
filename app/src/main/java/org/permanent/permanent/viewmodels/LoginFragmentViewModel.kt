package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.Validator
import org.permanent.permanent.models.Account
import org.permanent.permanent.models.Archive
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.repositories.*
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class LoginFragmentViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val errorMessage = MutableLiveData<String>()
    private val emailError = MutableLiveData<Int>()
    private val passwordError = MutableLiveData<Int>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onLoggedIn = SingleLiveEvent<Void>()
    private val onUserMissingDefaultArchive = SingleLiveEvent<Void>()
    private val onSignUp = SingleLiveEvent<Void>()
    private val onPasswordReset = SingleLiveEvent<Void>()
    private val onForgotPasswordRequest = SingleLiveEvent<Void>()
    private val currentEmail = MutableLiveData<String>()
    private val currentPassword = MutableLiveData<String>()
    val versionName = MutableLiveData(
        application.getString(
            R.string.version_text, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE.toString()
        )
    )
    private var authRepository: IAuthenticationRepository =
        AuthenticationRepositoryImpl(application)
    private val accountRepository: IAccountRepository = AccountRepositoryImpl(application)
    private val archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)

    fun login() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        currentEmail.value = currentEmail.value?.trim()
        currentPassword.value = currentPassword.value?.trim()

        val email = currentEmail.value
        val password = currentPassword.value

        if (!Validator.isValidEmail(null, email, emailError, null)) return
        if (!Validator.isValidPassword(password, passwordError)) return

        isBusy.value = true
        authRepository.login(email!!,
            password!!,
            object : IAuthenticationRepository.IOnLoginListener {
                override fun onSuccess() {
                    isBusy.value = false
                    prefsHelper.saveUserLoggedIn(true)

                    val defaultArchiveId = prefsHelper.getDefaultArchiveId()
                    if (defaultArchiveId == 0) {
                        onUserMissingDefaultArchive.call()
                    } else {
                        getArchive(defaultArchiveId)
                    }
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    when (error) {
                        Constants.ERROR_UNKNOWN_SIGNIN -> errorMessage.value =
                            appContext.getString(R.string.login_bad_credentials)
                        Constants.ERROR_SERVER_ERROR -> errorMessage.value =
                            appContext.getString(R.string.server_error)
                        else -> {
                            prefsHelper.saveAccountEmail(email) // We save this here for verifyCode
                            errorMessage.value = error
                        }
                    }
                }
            })
    }

    fun getArchive(defaultArchiveId: Int) {
        archiveRepository.getAllArchives(object : IDataListener {
            override fun onSuccess(dataList: List<Datum>?) {
                if (!dataList.isNullOrEmpty()) {
                    for (data in dataList) {
                        val archive = Archive(data.ArchiveVO)
                        if (defaultArchiveId == archive.id) {
                            prefsHelper.saveCurrentArchiveInfo(
                                archive.id,
                                archive.number,
                                archive.type,
                                archive.fullName,
                                archive.thumbURL200,
                                archive.accessRole
                            )
                            onLoggedIn.call()
                            return
                        }
                    }
                }
                errorMessage.value = appContext.getString(R.string.generic_error)
            }

            override fun onFailed(error: String?) {
                error?.let { errorMessage.value = it }
            }
        })
    }

    fun signUp() {
        onSignUp.call()
    }

    fun onForgotPasswordBtnClick() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        onForgotPasswordRequest.call()
    }

    fun getCurrentEmail(): MutableLiveData<String> = currentEmail

    fun getCurrentPassword(): MutableLiveData<String> = currentPassword

    fun getEmailError(): LiveData<Int> = emailError

    fun getPasswordError(): LiveData<Int> = passwordError

    fun onEmailTextChanged(email: Editable) {
        currentEmail.value = email.toString()
    }

    fun onPasswordTextChanged(password: Editable) {
        currentPassword.value = password.toString()
    }

    fun getErrorMessage(): LiveData<String> = errorMessage

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getOnUserMissingDefaultArchive(): MutableLiveData<Void> = onUserMissingDefaultArchive

    fun getOnLoggedIn(): MutableLiveData<Void> = onLoggedIn

    fun getOnSignUp(): MutableLiveData<Void> = onSignUp

    fun getOnPasswordReset(): MutableLiveData<Void> = onPasswordReset

    fun getOnForgotPasswordRequest(): MutableLiveData<Void> = onForgotPasswordRequest
}
