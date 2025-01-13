package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import co.infinum.goldfinger.Goldfinger
import co.infinum.goldfinger.MissingHardwareException
import co.infinum.goldfinger.NoEnrolledFingerprintException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.permanent.permanent.Constants
import org.permanent.permanent.EventsManager
import org.permanent.permanent.R
import org.permanent.permanent.Validator
import org.permanent.permanent.models.Account
import org.permanent.permanent.models.Archive
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.repositories.AccountRepositoryImpl
import org.permanent.permanent.repositories.ArchiveRepositoryImpl
import org.permanent.permanent.repositories.AuthenticationRepositoryImpl
import org.permanent.permanent.repositories.IAccountRepository
import org.permanent.permanent.repositories.IArchiveRepository
import org.permanent.permanent.repositories.IAuthenticationRepository
import org.permanent.permanent.repositories.INotificationRepository
import org.permanent.permanent.repositories.NotificationRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.login.compose.AuthPage

class AuthenticationViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val TAG = AuthenticationViewModel::class.java.simpleName
    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private var isTablet = false
    private val onSignedIn = SingleLiveEvent<Void?>()
    private val onAuthenticated = SingleLiveEvent<Void?>()
    private val onAccountCreated = SingleLiveEvent<Void?>()
    private val onUserMissingDefaultArchive = SingleLiveEvent<Void?>()
    private val onShowEnrollBiometricsDialog = SingleLiveEvent<Boolean>()
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

    private lateinit var promptParams: Goldfinger.PromptParams
    private var goldFinger = Goldfinger.Builder(appContext).build()

    private var savedEmail: String? = null
    private var savedPassword: String? = null

    private var authRepository: IAuthenticationRepository =
        AuthenticationRepositoryImpl(application)
    private val archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)
    private var accountRepository: IAccountRepository = AccountRepositoryImpl(application)

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
                prefsHelper.setIsTwoFAEnabled(false)

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
                        prefsHelper.setIsTwoFAEnabled(true)
                        prefsHelper.saveAccountEmail(email) //  Save email for verification
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
                            prefsHelper.saveUserLoggedIn(true)
                            onSignedIn.call()
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
                    onCodeVerified()
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

    fun onCodeVerified() {
        val defaultArchiveId = prefsHelper.getDefaultArchiveId()

        if (defaultArchiveId == 0) onUserMissingDefaultArchive.call()
        else getArchive(defaultArchiveId)
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

    fun skipLogin(): Boolean {
        return  prefsHelper.isUserLoggedIn()
                && !goldFinger.canAuthenticate()
                && !goldFinger.hasFingerprintHardware()
    }

    fun buildPromptParams(fragment: Fragment) {
        promptParams = Goldfinger.PromptParams.Builder(fragment)
            .title(R.string.login_biometric_title)
            .description(R.string.login_biometric_message)
            .deviceCredentialsAllowed(true)
            .negativeButtonText(R.string.button_cancel)
            .build()
    }

    fun authenticateUser() {
        goldFinger.authenticate(promptParams, object : Goldfinger.Callback {
            override fun onError(exception: Exception) {
                when (exception) {
                    is NoEnrolledFingerprintException -> handleResult(Goldfinger.Reason.NO_BIOMETRICS)
                    is MissingHardwareException -> handleResult(Goldfinger.Reason.HW_NOT_PRESENT)
                    else -> exception.message?.let { showErrorMessage(it) }
                }
            }
            override fun onResult(result: Goldfinger.Result) {
                if (result.type() == Goldfinger.Type.SUCCESS)
                    handleResult(Goldfinger.Reason.AUTHENTICATION_SUCCESS)
                else if (result.type() != Goldfinger.Type.INFO)
                    handleResult(result.reason())
            }
        })
    }

    fun handleResult(reason: Goldfinger.Reason) {
        var messageId = 0
        when (reason) {
            Goldfinger.Reason.CANCELED -> messageId = 0
            Goldfinger.Reason.USER_CANCELED -> messageId = 0
            Goldfinger.Reason.AUTHENTICATION_START -> messageId = 0
            Goldfinger.Reason.AUTHENTICATION_SUCCESS -> onAuthenticated.call()
            Goldfinger.Reason.NO_BIOMETRICS -> showOpenSettingsQuestionDialog()
            Goldfinger.Reason.HW_NOT_PRESENT ->
                messageId = R.string.login_biometric_error_no_biometric_hardware
            Goldfinger.Reason.HARDWARE_UNAVAILABLE ->
                messageId = R.string.login_biometric_error_unavailable
            Goldfinger.Reason.TIMEOUT ->
                messageId = R.string.login_biometric_error_timeout
            Goldfinger.Reason.LOCKOUT,
            Goldfinger.Reason.LOCKOUT_PERMANENT -> {
                messageId = R.string.login_biometric_error_too_many_failed_attempts
                deleteDeviceToken()
            }
            Goldfinger.Reason.NO_DEVICE_CREDENTIAL,
            Goldfinger.Reason.NEGATIVE_BUTTON,
            Goldfinger.Reason.UNABLE_TO_PROCESS,
            Goldfinger.Reason.VENDOR,
            Goldfinger.Reason.NO_SPACE,
            Goldfinger.Reason.AUTHENTICATION_FAIL,
            Goldfinger.Reason.UNKNOWN -> messageId = R.string.login_biometric_error_failed
        }
        if (messageId != 0) showErrorMessage(appContext.getString(messageId))
    }

    fun deleteDeviceToken() {
        if (_isBusyState.value) {
            return
        }

        _isBusyState.value = true
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    _isBusyState.value = false
                    Log.e(TAG, "Fetching FCM token failed: ${task.exception}")
                    return@OnCompleteListener
                }
                val notificationsRepository: INotificationRepository =
                    NotificationRepositoryImpl(appContext)

                notificationsRepository.deleteDevice(task.result, object : IResponseListener {

                    override fun onSuccess(message: String?) {
                        _isBusyState.value = false
                        logout()
                    }

                    override fun onFailed(error: String?) {
                        _isBusyState.value = false
                        Log.e(TAG, "Deleting Device FCM token failed: $error")
                        logout()
                    }
                })
            })
    }

    fun logout() {
        if (_isBusyState.value) {
            return
        }

        _isBusyState.value = true
        authRepository.logout(object : IAuthenticationRepository.IOnLogoutListener {
            override fun onSuccess() {
                _isBusyState.value = false
                EventsManager(appContext).resetUser()
                _navigateToPage.value = AuthPage.SIGN_IN
            }

            override fun onFailed(error: String?) {
                _isBusyState.value = false
                when (error) {
                    Constants.ERROR_SERVER_ERROR,
                    Constants.ERROR_NO_API_KEY -> showErrorMessage(appContext.getString(R.string.server_error))
                    else -> error?.let { showErrorMessage(it) }
                }
            }
        })
    }

    private fun showOpenSettingsQuestionDialog() {
        onShowEnrollBiometricsDialog.value = true
    }

    fun clearSnackbar() {
        _snackbarMessage.value = ""
    }

    fun setNavigateToPage(page: AuthPage) {
        _navigateToPage.value = page
    }

    fun clearPageNavigation() {
        _navigateToPage.value = null
    }

    fun isTablet() = isTablet

    fun getOnUserMissingDefaultArchive(): MutableLiveData<Void?> = onUserMissingDefaultArchive

    fun getOnShowEnrollBiometricsDialog(): MutableLiveData<Boolean> = onShowEnrollBiometricsDialog

    fun getOnSignedIn(): MutableLiveData<Void?> = onSignedIn

    fun getOnAuthenticated(): MutableLiveData<Void?> = onAuthenticated

    fun getOnAccountCreated(): MutableLiveData<Void?> = onAccountCreated
}
