package org.permanent.permanent.viewmodels

import android.app.Application
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.AccountRepositoryImpl
import org.permanent.permanent.repositories.IAccountRepository
import org.permanent.permanent.ui.composeComponents.SnackbarType

class ChangePasswordViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private var appContext = application.applicationContext

    private val _isBusyState = MutableStateFlow(false)
    val isBusyState: StateFlow<Boolean> = _isBusyState
    private val _snackbarMessage = MutableStateFlow("")
    val snackbarMessage: StateFlow<String> = _snackbarMessage
    private val _snackbarType = MutableStateFlow(SnackbarType.NONE)
    val snackbarType: StateFlow<SnackbarType> = _snackbarType
    private var accountRepository: IAccountRepository = AccountRepositoryImpl(application)

    fun changePassword(currentPassword: String, newPassword: String, retypedNewPassword: String, onSuccess: () -> Unit = {}) {
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
}