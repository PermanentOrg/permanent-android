package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.Validator
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.ITwoFAListener
import org.permanent.permanent.network.models.TwoFAVO
import org.permanent.permanent.repositories.AuthenticationRepositoryImpl
import org.permanent.permanent.repositories.IAuthenticationRepository
import org.permanent.permanent.repositories.StelaAccountRepository
import org.permanent.permanent.repositories.StelaAccountRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.composeComponents.SnackbarType
import org.permanent.permanent.ui.settings.compose.twoStepVerification.VerificationMethod

class LoginAndSecurityViewModel(application: Application) :
    ObservableAndroidViewModel(application) {
    private val appContext = application.applicationContext

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

    private val _isBiometricsEnabled = MutableStateFlow(prefsHelper.isBiometricsLogIn())
    val isBiometricsEnabled: StateFlow<Boolean> = _isBiometricsEnabled

    private val _isTwoFAEnabled = MutableStateFlow(prefsHelper.isTwoFAEnabled())
    val isTwoFAEnabled: StateFlow<Boolean> = _isTwoFAEnabled

    private val _twoFAList = MutableStateFlow(prefsHelper.getTwoFAList())
    val twoFAList: StateFlow<List<TwoFAVO>> = _twoFAList

    private var twoFAMethodToDisable = TwoFAVO()
    private val _isChangeVerificationMethod = MutableStateFlow(false)
    val isChangeVerificationMethod: StateFlow<Boolean> = _isChangeVerificationMethod

    private val _codeValues = MutableStateFlow(List(4) { "" })
    val codeValues: StateFlow<List<String>> = _codeValues

    private var authRepository: IAuthenticationRepository =
        AuthenticationRepositoryImpl(application)
    private var stelaAccountRepository: StelaAccountRepository =
        StelaAccountRepositoryImpl(application)

    fun updateBiometricsEnabled(enabled: Boolean) {
        _isBiometricsEnabled.value = enabled
        prefsHelper.setBiometricsLogIn(enabled)
    }

    fun updateTwoFAEnabled(enabled: Boolean) {
        prefsHelper.setIsTwoFAEnabled(enabled)
        _isTwoFAEnabled.value = enabled
    }

    fun updateTwoFAList(newList: List<TwoFAVO>) {
        prefsHelper.setTwoFAList(newList)
        _twoFAList.value = newList
    }

    fun updateTwoFAMethodToDisable(twoFAVO: TwoFAVO) {
        twoFAMethodToDisable = twoFAVO
    }

    fun setIsChangeVerificationMethod(value: Boolean) {
        _isChangeVerificationMethod.value = value
    }

    fun verifyPassword(password: String, successCallback: () -> Unit) {
        if (_isBusyState.value) {
            return
        }
        if (!Validator.isValidPassword(password, null)) {
            _snackbarMessage.value = appContext.getString(R.string.the_entered_data_is_invalid)
            _snackbarType.value = SnackbarType.ERROR
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
                        successCallback()
                    }

                    override fun onFailed(error: String?) {
                        _isBusyState.value = false
                        when (error) {
                            Constants.ERROR_UNKNOWN_SIGNIN -> {
                                _snackbarMessage.value =
                                    appContext.getString(R.string.incorrect_password)
                                _snackbarType.value = SnackbarType.ERROR
                            }

                            Constants.ERROR_SERVER_ERROR -> {
                                _snackbarMessage.value = appContext.getString(R.string.server_error)
                                _snackbarType.value = SnackbarType.ERROR
                            }

                            Constants.ERROR_MFA_TOKEN -> {
                                successCallback()
                            }

                            else -> {
                                if (error != null) {
                                    _snackbarMessage.value = error
                                    _snackbarType.value = SnackbarType.ERROR
                                } else {
                                    _snackbarMessage.value =
                                        appContext.getString(R.string.generic_error)
                                    _snackbarType.value = SnackbarType.ERROR
                                }
                            }
                        }
                    }
                })
        }
    }

    private fun updateTwoFA(successCallback: () -> Unit) {
        stelaAccountRepository.getTwoFAMethod(object : ITwoFAListener {

            override fun onSuccess(twoFAVOList: List<TwoFAVO>?) {
                updateTwoFAEnabled(!twoFAVOList.isNullOrEmpty())
                updateTwoFAList(twoFAVOList ?: emptyList())
                successCallback()
            }

            override fun onFailed(error: String?) {
                error?.let {
                    _snackbarMessage.value = it
                } ?: run { _snackbarMessage.value = appContext.getString(R.string.generic_error) }
                _snackbarType.value = SnackbarType.ERROR
            }
        })
    }


    fun sendEnableCode(method: VerificationMethod, value: String, successCallback: () -> Unit) {
        if (_isBusyState.value) {
            return
        }
        clearSnackbar()
        _isBusyState.value = true
        val twoFAVO = TwoFAVO(method = method.name.lowercase(), value = value)
        stelaAccountRepository.sendEnableCode(twoFAVO, object : IResponseListener {

            override fun onSuccess(message: String?) {
                _isBusyState.value = false
                successCallback()
                _snackbarMessage.value = appContext.getString(R.string.code_sent)
                _snackbarType.value = SnackbarType.SUCCESS
            }

            override fun onFailed(error: String?) {
                _isBusyState.value = false
                error?.let {
                    _snackbarMessage.value = it
                    _snackbarType.value = SnackbarType.ERROR
                }
            }
        })
    }

    fun enableTwoFactor(
        method: VerificationMethod, value: String, code: String, successCallback: () -> Unit
    ) {
        if (_isBusyState.value) {
            return
        }
        clearSnackbar()
        _isBusyState.value = true
        val twoFAVO = TwoFAVO(method = method.name.lowercase(), value = value, code = code)
        stelaAccountRepository.enableTwoFactor(twoFAVO, object : IResponseListener {

            override fun onSuccess(message: String?) {
                _isBusyState.value = false

                val twoFAListSaved = prefsHelper.getTwoFAList()
                val updatedList = twoFAListSaved.toMutableList()
                updatedList.add(twoFAVO)

                updateTwoFAEnabled(true)
                updateTwoFAList(updatedList)
                updateCodeValues(List(4) { "" })
                successCallback()
            }

            override fun onFailed(error: String?) {
                _isBusyState.value = false
                error?.let {
                    _snackbarMessage.value = it
                    _snackbarType.value = SnackbarType.ERROR
                }
            }
        })
    }

    fun sendTwoFADisableCode(successCallback: () -> Unit) {
        if (_isBusyState.value) {
            return
        }
        clearSnackbar()
        _isBusyState.value = true
        if (twoFAMethodToDisable.methodId == null) {
            updateTwoFA(successCallback = {
                val matchingTwoFA = twoFAList.value.find { it.method == twoFAMethodToDisable.method }
                if (matchingTwoFA != null) {
                    twoFAMethodToDisable = matchingTwoFA
                    sendDisableCode(successCallback)
                }
            })
        } else {
            sendDisableCode(successCallback)
        }
    }

    private fun sendDisableCode(successCallback: () -> Unit) {
        val twoFAVO = TwoFAVO(methodId = twoFAMethodToDisable.methodId)
        stelaAccountRepository.sendDisableCode(twoFAVO, object : IResponseListener {

            override fun onSuccess(message: String?) {
                _isBusyState.value = false
                successCallback()
                _snackbarMessage.value = appContext.getString(R.string.code_sent)
                _snackbarType.value = SnackbarType.SUCCESS
            }

            override fun onFailed(error: String?) {
                _isBusyState.value = false
                error?.let {
                    _snackbarMessage.value = it
                    _snackbarType.value = SnackbarType.ERROR
                }
            }
        })
    }

    fun disableTwoFactor(code: String, successCallback: () -> Unit) {
        if (_isBusyState.value) {
            return
        }
        clearSnackbar()
        _isBusyState.value = true
        val twoFAVO = TwoFAVO(methodId = twoFAMethodToDisable.methodId, code = code)
        stelaAccountRepository.disableTwoFactor(twoFAVO, object : IResponseListener {

            override fun onSuccess(message: String?) {
                _isBusyState.value = false

                val twoFAVOList = twoFAList.value.toMutableList()
                twoFAVOList.removeIf { it.methodId == twoFAMethodToDisable.methodId }
                updateTwoFAEnabled(twoFAVOList.isNotEmpty())
                updateTwoFAList(twoFAVOList)
                updateCodeValues(List(4) { "" })
                successCallback()

                _snackbarMessage.value =
                    if (twoFAMethodToDisable.method == VerificationMethod.EMAIL.name.lowercase()) appContext.getString(
                        R.string.email_verification_disabled
                    )
                    else appContext.getString(R.string.text_verification_disabled)
                _snackbarType.value = SnackbarType.SUCCESS
                twoFAMethodToDisable = TwoFAVO()
            }

            override fun onFailed(error: String?) {
                _isBusyState.value = false
                error?.let {
                    _snackbarMessage.value = it
                    _snackbarType.value = SnackbarType.ERROR
                }
            }
        })
    }

    fun updateCodeValues(newValues: List<String>) {
        _codeValues.value = newValues
    }

    fun clearSnackbar() {
        _snackbarMessage.value = ""
    }

    fun isTablet() = isTablet
}