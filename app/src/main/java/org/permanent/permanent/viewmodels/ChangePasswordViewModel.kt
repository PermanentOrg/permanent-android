package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.compose.ui.platform.SoftwareKeyboardController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.AccountRepositoryImpl
import org.permanent.permanent.repositories.IAccountRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.composeComponents.SnackbarType

class ChangePasswordViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private var appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private var isTablet = prefsHelper.isTablet()
    private val _isBusyState = MutableStateFlow(false)
    val isBusyState: StateFlow<Boolean> = _isBusyState
    private val _snackbarMessage = MutableStateFlow("")
    val snackbarMessage: StateFlow<String> = _snackbarMessage
    private val _snackbarType = MutableStateFlow(SnackbarType.NONE)
    val snackbarType: StateFlow<SnackbarType> = _snackbarType
    private var accountRepository: IAccountRepository = AccountRepositoryImpl(application)

    fun handleChangePassword(
        currentPassword: String,
        newPassword: String,
        retypedNewPassword: String,
        keyboardController: SoftwareKeyboardController?,
        onSuccess: () -> Unit
    ) {
        keyboardController?.hide()
        when {
            currentPassword.isBlank() -> {
                showSnackbar(
                    message = appContext.getString(R.string.current_password_required),
                    type = SnackbarType.ERROR
                )
            }

            newPassword.length < 8 -> {
                showSnackbar(
                    message = appContext.getString(R.string.password_min_length_error),
                    type = SnackbarType.ERROR
                )
            }

            newPassword != retypedNewPassword -> {
                showSnackbar(
                    message = appContext.getString(R.string.passwords_do_not_match),
                    type = SnackbarType.ERROR
                )
            }

            newPassword == currentPassword -> {
                showSnackbar(
                    message = appContext.getString(R.string.new_password_same_as_current),
                    type = SnackbarType.ERROR
                )
            }

            else -> {
                changePassword(
                    currentPassword, newPassword, retypedNewPassword, onSuccess = onSuccess
                )
            }
        }
    }

    private fun changePassword(currentPassword: String, newPassword: String, retypedNewPassword: String, onSuccess: () -> Unit = {}) {
        if (_isBusyState.value) {
            return
        }

        _isBusyState.value = true
        accountRepository.changePassword(currentPassword, newPassword, retypedNewPassword,
            object : IResponseListener {
                override fun onSuccess(message: String?) {
                    _isBusyState.value = false
                    _snackbarMessage.value = message ?: appContext.getString(R.string.generic_error)
                    _snackbarType.value = SnackbarType.SUCCESS
                    onSuccess()
                }

                override fun onFailed(error: String?) {
                    _isBusyState.value = false
                    _snackbarMessage.value = if (error == null || error == Constants.ERROR_GENERIC_INTERNAL) appContext.getString(R.string.generic_error) else error
                    _snackbarType.value = SnackbarType.ERROR
                }
            }
        )
    }

    fun showSnackbar(message: String, type: SnackbarType = SnackbarType.ERROR) {
        _snackbarMessage.value = message
        _snackbarType.value = type
    }

    fun clearSnackbar() {
        _snackbarMessage.value = ""
    }

    fun isTablet() = isTablet
}