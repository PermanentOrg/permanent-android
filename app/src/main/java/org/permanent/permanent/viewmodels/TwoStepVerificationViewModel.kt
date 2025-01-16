package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.Validator
import org.permanent.permanent.network.models.TwoFAVO
import org.permanent.permanent.repositories.AuthenticationRepositoryImpl
import org.permanent.permanent.repositories.IAuthenticationRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class TwoStepVerificationViewModel(application: Application) :
    ObservableAndroidViewModel(application) {
    private val appContext = application.applicationContext

    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    private val _isBusyState = MutableStateFlow(false)
    val isBusyState: StateFlow<Boolean> = _isBusyState
    private val _snackbarMessage = MutableStateFlow("")
    val snackbarMessage: StateFlow<String> = _snackbarMessage

    private val _isTwoFAEnabled = MutableStateFlow(prefsHelper.isTwoFAEnabled())
    val isTwoFAEnabled: StateFlow<Boolean> = _isTwoFAEnabled

    private val _twoFAList = MutableStateFlow(prefsHelper.getTwoFAList())
    val twoFAList: StateFlow<List<TwoFAVO>> = _twoFAList

    private var authRepository: IAuthenticationRepository =
        AuthenticationRepositoryImpl(application)

    fun updateTwoFAList(newList: List<TwoFAVO>) {
        prefsHelper.setTwoFAList(newList)
        _twoFAList.value = newList
    }

    fun verifyPassword(password: String, errorMessageCallback: (String?) -> Unit) {
        if (_isBusyState.value) {
            return
        }
        if (!Validator.isValidPassword(password, null)) {
            _snackbarMessage.value = appContext.getString(R.string.the_entered_data_is_invalid)
            errorMessageCallback(appContext.getString(R.string.the_entered_data_is_invalid))
            return
        }

        val email = prefsHelper.getAccountEmail()
        if (email != null) {
            clearSnackbar()
            _isBusyState.value = true
            authRepository.login(
                email,
                password,
                object : IAuthenticationRepository.IOnLoginListener {
                    override fun onSuccess() {
                        _isBusyState.value = false
                        errorMessageCallback(null)
                    }

                    override fun onFailed(error: String?) {
                        _isBusyState.value = false
                        when (error) {
                            Constants.ERROR_UNKNOWN_SIGNIN -> {
                                _snackbarMessage.value =
                                    appContext.getString(R.string.incorrect_password)
                                errorMessageCallback(
                                    appContext.getString(R.string.incorrect_password)
                                )
                            }

                            Constants.ERROR_SERVER_ERROR -> {
                                _snackbarMessage.value = appContext.getString(R.string.server_error)
                                errorMessageCallback(
                                    appContext.getString(R.string.server_error)
                                )
                            }

                            Constants.ERROR_MFA_TOKEN -> {
                                errorMessageCallback(null)
                            }

                            else -> {
                                if (error != null) {
                                    _snackbarMessage.value = error
                                    errorMessageCallback(error)
                                } else {
                                    _snackbarMessage.value =
                                        appContext.getString(R.string.generic_error)
                                    errorMessageCallback(
                                        appContext.getString(R.string.generic_error)
                                    )
                                }
                            }
                        }
                    }
                })
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = ""
    }
}