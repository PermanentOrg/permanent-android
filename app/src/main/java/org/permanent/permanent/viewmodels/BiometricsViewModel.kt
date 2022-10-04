package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import co.infinum.goldfinger.Goldfinger
import co.infinum.goldfinger.MissingHardwareException
import co.infinum.goldfinger.NoEnrolledFingerprintException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.AuthenticationRepositoryImpl
import org.permanent.permanent.repositories.IAuthenticationRepository
import org.permanent.permanent.repositories.INotificationRepository
import org.permanent.permanent.repositories.NotificationRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class BiometricsViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val TAG = BiometricsViewModel::class.java.simpleName
    private val appContext = application.applicationContext
    private var prefsHelper = PreferencesHelper(appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))
    private var goldFinger = Goldfinger.Builder(appContext).build()
    private val isBusy = MutableLiveData<Boolean>()
    private val onNavigateToMainActivity = SingleLiveEvent<Void>()
    private val onLoggedOut = SingleLiveEvent<Void>()
    private val onShowOpenSettingsQuestionDialog = SingleLiveEvent<Void>()
    private val errorMessage = MutableLiveData<String>()
    private val errorStringId = MutableLiveData<Int>()
    private var authRepository: IAuthenticationRepository = AuthenticationRepositoryImpl(application)
    private lateinit var promptParams: Goldfinger.PromptParams

    init {
        if(skipLogin()) onNavigateToMainActivity.call()
        else if (skipBiometrics()) onLoggedOut.call()
    }

    private fun skipLogin(): Boolean {
        return  prefsHelper.isUserLoggedIn()
                && !goldFinger.canAuthenticate()
                && !goldFinger.hasFingerprintHardware()
    }

    private fun skipBiometrics(): Boolean {
        return !prefsHelper.isUserLoggedIn()
    }

    fun buildPromptParams(fragment: Fragment) {
        promptParams = Goldfinger.PromptParams.Builder(fragment)
            .title(R.string.login_biometric_title)
            .description(R.string.login_biometric_message)
            .deviceCredentialsAllowed(true)
            .negativeButtonText(R.string.button_cancel)
            .build()
    }

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getOnNavigateToMainActivity(): LiveData<Void> = onNavigateToMainActivity

    fun getOnLoggedOut(): LiveData<Void> = onLoggedOut

    fun getOnShowOpenSettingsQuestionDialog(): LiveData<Void> = onShowOpenSettingsQuestionDialog

    fun getErrorMessage(): LiveData<String> = errorMessage

    fun getErrorStringId(): LiveData<Int> = errorStringId

    fun authenticateUser() {
        goldFinger.authenticate(promptParams, object : Goldfinger.Callback {
            override fun onError(exception: Exception) {
                when (exception) {
                    is NoEnrolledFingerprintException -> handleResult(Goldfinger.Reason.NO_BIOMETRICS)
                    is MissingHardwareException -> handleResult(Goldfinger.Reason.HW_NOT_PRESENT)
                    else -> exception.message?.let { errorMessage.value = it }
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
            Goldfinger.Reason.AUTHENTICATION_SUCCESS -> onNavigateToMainActivity.call()
            Goldfinger.Reason.NO_BIOMETRICS -> onShowOpenSettingsQuestionDialog.call()
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
        if (messageId != 0) errorStringId.value = messageId
    }

    fun deleteDeviceToken() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    isBusy.value = false
                    Log.e(TAG, "Fetching FCM token failed: ${task.exception}")
                    return@OnCompleteListener
                }
                val notificationsRepository: INotificationRepository =
                    NotificationRepositoryImpl(appContext)

                notificationsRepository.deleteDevice(task.result, object : IResponseListener {

                        override fun onSuccess(message: String?) {
                            isBusy.value = false
                            logout()
                        }

                        override fun onFailed(error: String?) {
                            isBusy.value = false
                            errorMessage.value = error
                            Log.e(TAG, "Deleting Device FCM token failed: $error")
                        }
                    })
            })
    }

    fun logout() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        authRepository.logout(object : IAuthenticationRepository.IOnLogoutListener {
            override fun onSuccess() {
                isBusy.value = false
                onLoggedOut.call()
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                when (error) {
                    Constants.ERROR_SERVER_ERROR,
                    Constants.ERROR_NO_API_KEY -> errorStringId.value = R.string.server_error
                    else -> errorMessage.value = error
                }
            }
        })
    }
}