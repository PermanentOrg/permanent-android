package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.Validator
import org.permanent.permanent.models.Account
import org.permanent.permanent.models.Archive
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.repositories.AccountRepositoryImpl
import org.permanent.permanent.repositories.ArchiveRepositoryImpl
import org.permanent.permanent.repositories.AuthenticationRepositoryImpl
import org.permanent.permanent.repositories.IAccountRepository
import org.permanent.permanent.repositories.IArchiveRepository
import org.permanent.permanent.repositories.IAuthenticationRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.login.compose.AuthPage

class AuthenticationViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private var isTablet = false
    private val onLoggedIn = SingleLiveEvent<Void?>()
    private val onAccountCreated = SingleLiveEvent<Void?>()
    private val onUserMissingDefaultArchive = SingleLiveEvent<Void?>()
    private val _isBusyState = MutableStateFlow(false)
    val isBusyState: StateFlow<Boolean> = _isBusyState
    private val _codeValues = MutableStateFlow(List(4) { "" })
    val codeValues: StateFlow<List<String>> = _codeValues
    private val _snackbarMessage = MutableStateFlow("")
    val snackbarMessage: StateFlow<String> = _snackbarMessage
    private val _snackbarType = MutableStateFlow(SnackbarType.NONE)
    val snackbarType: StateFlow<SnackbarType> = _snackbarType
    private val _navigateToPage = MutableStateFlow<AuthPage?>(null)
    val navigateToPage: StateFlow<AuthPage?> = _navigateToPage

    private var savedEmail: String? = null
    private var savedPassword: String? = null

    private var authRepository: IAuthenticationRepository =
        AuthenticationRepositoryImpl(application)
    private val archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)
    private var accountRepository: IAccountRepository = AccountRepositoryImpl(application)
//    private var stelaAccountRepository: StelaAccountRepository =
//        StelaAccountRepositoryImpl(application)

    enum class SnackbarType {
        SUCCESS, ERROR, NONE
    }

    init {
        isTablet = prefsHelper.isTablet()
    }

    fun login(withNavigation: Boolean, email: String, password: String) {
        if (_isBusyState.value) {
            return
        }

        if (!Validator.isValidEmail(null, email, null, null) || !Validator.isValidPassword(
                password, null
            )
        ) {
            showErrorMessage(appContext.getString(R.string.the_entered_data_is_invalid))
            return
        }

        savedEmail = email
        savedPassword = password

        _isBusyState.value = true
        authRepository.login(email, password, object : IAuthenticationRepository.IOnLoginListener {
            override fun onSuccess() {
                _isBusyState.value = false
                prefsHelper.saveUserLoggedIn(true)

                val defaultArchiveId = prefsHelper.getDefaultArchiveId()
                if (defaultArchiveId == 0) {
                    onUserMissingDefaultArchive.call()
                } else {
                    getArchive(defaultArchiveId)
                }
            }

            override fun onFailed(error: String?) {
                _isBusyState.value = false
                when (error) {
                    Constants.ERROR_UNKNOWN_SIGNIN -> showErrorMessage(
                        appContext.getString(R.string.login_bad_credentials)
                    )

                    Constants.ERROR_SERVER_ERROR -> showErrorMessage(
                        appContext.getString(R.string.server_error)
                    )

                    Constants.ERROR_MFA_TOKEN -> {
                        prefsHelper.saveAccountEmail(email) //  Save email for verification
//                        getTwoFAMethod()
                        if (withNavigation) _navigateToPage.value = AuthPage.CODE_VERIFICATION
                        else showSuccessMessage(appContext.getString(R.string.code_resent))
                    }

                    else -> {
                        if (error != null) {
                            showErrorMessage(error)
                        }
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
                showErrorMessage(appContext.getString(R.string.generic_error))
            }

            override fun onFailed(error: String?) {
                error?.let { showErrorMessage(it) }
            }
        })
    }

//    fun getTwoFAMethod() {
//        stelaAccountRepository.getTwoFAMethod(object : IResponseListener {
//
//            override fun onSuccess(message: String?) {
//                _isBusyState.value = false
//                _navigateToPage.value = AuthPage.CODE_VERIFICATION
//            }
//
//            override fun onFailed(error: String?) {
//                _isBusyState.value = false
//                error?.let { showErrorMessage(it) }
//            }
//        })
//    }

    fun resendCode() {
        val email = savedEmail
        val password = savedPassword

        if (email != null && password != null) {
            login(false, email, password)
        } else {
            showErrorMessage(appContext.getString(R.string.generic_error))
        }
    }

    fun verifyCode(code: String, onCleared: () -> Unit) {
        if (_isBusyState.value) {
            return
        }
        if (code.length < 4) {
            showErrorMessage(appContext.getString(R.string.code_is_incorrect))
            return
        }

        _isBusyState.value = true
        authRepository.verifyCode(code,
            Constants.AUTH_TYPE_MFA_VALIDATION,
            object : IAuthenticationRepository.IOnVerifyListener {
                override fun onSuccess() {
                    _isBusyState.value = false
                    onLoggedIn.call()
                }

                override fun onFailed(error: String?) {
                    _isBusyState.value = false
                    clearCodeValues(onCleared)
                    showErrorMessage(
                        if (error.equals(Constants.ERROR_INVALID_VERIFICATION_CODE)) {
                            appContext.getString(R.string.code_is_incorrect)
                        } else if (error.equals(Constants.ERROR_EXPIRED_VERIFICATION_CODE)) {
                            appContext.getString(R.string.code_expired)
                        } else error ?: ""
                    )
                }
            })
    }

    fun updateCodeValues(newValues: List<String>) {
        _codeValues.value = newValues
    }

    private fun clearCodeValues(onCleared: () -> Unit) {
        viewModelScope.launch {
            _codeValues.value = List(4) { "" }
            // Small delay to ensure the state update is propagated before requesting focus
            delay(100)
            onCleared() // Trigger the focus shift after the state update is fully reflected
        }
    }

    fun forgotPassword(email: String) {
        if (_isBusyState.value) {
            return
        }

        if (!Validator.isValidEmail(null, email, null, null)) {
            showErrorMessage(appContext.getString(R.string.the_entered_data_is_invalid))
            return
        }

        _isBusyState.value = true
        authRepository.forgotPassword(
            email,
            object : IAuthenticationRepository.IOnResetPasswordListener {
                override fun onSuccess() {
                    _isBusyState.value = false
                    _navigateToPage.value = AuthPage.FORGOT_PASSWORD_DONE
                }

                override fun onFailed(error: String?) {
                    _isBusyState.value = false
                    error?.let {
                        if (it.contains(Constants.ERROR_GENERIC_INTERNAL)) _navigateToPage.value =
                            AuthPage.FORGOT_PASSWORD_DONE
                        else showErrorMessage(it)
                    }
                }
            })
    }

    fun showSuccessMessage(message: String) {
        clearSnackbar()
        // Post the new message with a small delay to allow UI refresh
        viewModelScope.launch {
            delay(50)
            _snackbarMessage.value = message
            _snackbarType.value = SnackbarType.SUCCESS
        }
    }

    fun showErrorMessage(message: String) {
        clearSnackbar()
        // Post the new message with a small delay to allow UI refresh
        viewModelScope.launch {
            delay(50)
            _snackbarMessage.value = message
            _snackbarType.value = SnackbarType.ERROR
        }
    }

    fun signUp(fullName: String, email: String, password: String, optIn: Boolean) {
        if (_isBusyState.value) {
            return
        }

        if (fullName.isEmpty() || !Validator.isValidEmail(
                null,
                email,
                null,
                null
            ) || !Validator.isValidPassword(
                password, null
            )
        ) {
            showErrorMessage(appContext.getString(R.string.the_entered_data_is_invalid))
            return
        }

        _isBusyState.value = true
        accountRepository.signUp(
            fullName,
            email,
            password,
            optIn,
            object : IAccountRepository.IAccountListener {

                override fun onSuccess(account: Account) {
                    _isBusyState.value = false

                    prefsHelper.saveAuthToken(account.token)
                    prefsHelper.saveAccountInfo(
                        account.id,
                        account.primaryEmail,
                        password,
                        account.fullName
                    )
                    prefsHelper.saveDefaultArchiveId(account.defaultArchiveId)

                    onAccountCreated.call()
                }

                override fun onFailed(error: String?) {
                    _isBusyState.value = false
                    error?.let { showErrorMessage(it) }
                }
            })
    }

    fun clearSnackbar() {
        _snackbarMessage.value = ""
    }

    fun clearPageNavigation() {
        _navigateToPage.value = null
    }

    fun isTablet() = isTablet

    fun getOnUserMissingDefaultArchive(): MutableLiveData<Void?> = onUserMissingDefaultArchive

    fun getOnLoggedIn(): MutableLiveData<Void?> = onLoggedIn

    fun getOnAccountCreated(): MutableLiveData<Void?> = onAccountCreated
}
