package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.ClientSecretBasic
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.R
import org.permanent.permanent.models.Account
import org.permanent.permanent.models.Archive
import org.permanent.permanent.network.AuthStateManager
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.repositories.*
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class SplashViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val onUserLoggedIn = SingleLiveEvent<Void>()
    private val onArchiveSwitchedToCurrent = SingleLiveEvent<Void>()
    private val showError = MutableLiveData<String>()
    private val authRepository: IAuthenticationRepository =
        AuthenticationRepositoryImpl(application)
    private val accountRepository: IAccountRepository = AccountRepositoryImpl(application)
    private val archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)

    fun requestTokens(authResponse: AuthorizationResponse) {
        val clientAuth = ClientSecretBasic(BuildConfig.AUTH_CLIENT_SECRET)
        val authService = AuthorizationService(appContext)

        authService.performTokenRequest(
            authResponse.createTokenExchangeRequest(), clientAuth
        ) { tokenResp, tokenEx ->
            AuthStateManager.getInstance(appContext).updateAfterTokenResponse(tokenResp, tokenEx)

            if (tokenResp != null) { // exchange succeeded
                prefsHelper.saveUserLoggedIn(true)
                verifyLoggedIn()
            } else {
                showError.value = tokenEx?.errorDescription
            }
        }
    }

    private fun verifyLoggedIn() {
        authRepository.verifyLoggedIn(object : IAuthenticationRepository.IOnLoggedInListener {
            override fun onResponse(isLoggedIn: Boolean) {
                getAccount()
            }
        })
    }

    fun getAccount() {
        accountRepository.getSessionAccount(object : IAccountRepository.IAccountListener {

            override fun onSuccess(account: Account) {
                prefsHelper.saveAccountInfo(account.id, account.primaryEmail, account.fullName)
                prefsHelper.saveDefaultArchiveId(account.defaultArchiveId)

                account.defaultArchiveId?.let { getArchive(it) } ?: run { onUserLoggedIn.call() }
            }

            override fun onFailed(error: String?) {
                error?.let { showError.value = it }
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
                            onUserLoggedIn.call()
                            return
                        }
                    }
                }
                showError.value = appContext.getString(R.string.generic_error)
            }

            override fun onFailed(error: String?) {
                error?.let { showError.value = it }
            }
        })
    }

    fun switchArchiveToCurrent() {
        prefsHelper.getCurrentArchiveNr()?.let { currentArchiveNr ->
            archiveRepository.switchToArchive(currentArchiveNr, object : IDataListener {
                override fun onSuccess(dataList: List<Datum>?) {
                    onArchiveSwitchedToCurrent.call()
                }

                override fun onFailed(error: String?) {
                    error?.let { showError.value = it }
                }
            })
        }
    }

    fun getOnUserLoggedIn(): MutableLiveData<Void> = onUserLoggedIn
    fun getOnArchiveSwitchedToCurrent(): MutableLiveData<Void> = onArchiveSwitchedToCurrent
    fun getShowError(): LiveData<String> = showError
}