package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.Validator
import org.permanent.permanent.models.Archive
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.repositories.ArchiveRepositoryImpl
import org.permanent.permanent.repositories.AuthenticationRepositoryImpl
import org.permanent.permanent.repositories.IArchiveRepository
import org.permanent.permanent.repositories.IAuthenticationRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class LoginFragmentViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private var isTablet = false
    private val onLoggedIn = SingleLiveEvent<Void?>()
    private val onUserMissingDefaultArchive = SingleLiveEvent<Void?>()
    private val _isBusyState = MutableStateFlow(false)
    val isBusyState: StateFlow<Boolean> = _isBusyState
    private val _showError = MutableStateFlow("")
    val showError: StateFlow<String> = _showError

    private var authRepository: IAuthenticationRepository =
        AuthenticationRepositoryImpl(application)
    private val archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)

    init {
        isTablet = prefsHelper.isTablet()
    }

    fun login(email: String, password: String) {
        if (_isBusyState.value) {
            return
        }

        if (!Validator.isValidEmail(null, email, null, null) || !Validator.isValidPassword(
                password, null)) {
            _showError.value = appContext.getString(R.string.the_entered_data_is_invalid)
            return
        }

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
                    Constants.ERROR_UNKNOWN_SIGNIN -> _showError.value =
                        appContext.getString(R.string.login_bad_credentials)

                    Constants.ERROR_SERVER_ERROR -> _showError.value =
                        appContext.getString(R.string.server_error)

                    Constants.ERROR_MFA_TOKEN -> {
                        prefsHelper.saveAccountEmail(email) // We save this here for verifyCode
                        // TODO: move to verifyCode page
                    }
                    else -> {
                        if (error != null) {
                            _showError.value = error
                        }
                    }
                }
            }
        })
    }

    fun clearError() {
        _showError.value = ""
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
                _showError.value = appContext.getString(R.string.generic_error)
            }

            override fun onFailed(error: String?) {
                error?.let { _showError.value = it }
            }
        })
    }

    fun isTablet() = isTablet

    fun getOnUserMissingDefaultArchive(): MutableLiveData<Void?> = onUserMissingDefaultArchive

    fun getOnLoggedIn(): MutableLiveData<Void?> = onLoggedIn
}
