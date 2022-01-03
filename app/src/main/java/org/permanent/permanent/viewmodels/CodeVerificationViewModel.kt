package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.Constants
import org.permanent.permanent.PermanentApplication
import org.permanent.permanent.R
import org.permanent.permanent.models.Archive
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.repositories.ArchiveRepositoryImpl
import org.permanent.permanent.repositories.AuthenticationRepositoryImpl
import org.permanent.permanent.repositories.IArchiveRepository
import org.permanent.permanent.repositories.IAuthenticationRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class CodeVerificationViewModel(application: Application) :
    ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    var isSmsCodeFlow = false
    private val currentCode = MutableLiveData<String>()
    private val codeError = MutableLiveData<Int>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onCodeVerified = SingleLiveEvent<Void>()
    private val onLoggedIn = SingleLiveEvent<Void>()
    private val errorMessage = MutableLiveData<String>()
    private var authRepository: IAuthenticationRepository =
        AuthenticationRepositoryImpl(application)
    private var archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)

    fun getVerificationCode(): MutableLiveData<String> {
        return currentCode
    }

    fun onCurrentCodeChanged(code: Editable) {
        currentCode.value = code.toString()
    }

    fun getCodeError(): LiveData<Int> {
        return codeError
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getOnCodeVerified(): MutableLiveData<Void> = onCodeVerified

    fun getOnLoggedIn(): MutableLiveData<Void> = onLoggedIn

    fun getErrorMessage(): LiveData<String> {
        return errorMessage
    }

    private fun isCodeValid(): Boolean {
        currentCode.value = currentCode.value?.trim()
        val trimmedCode = currentCode.value

        if (trimmedCode.isNullOrEmpty()) {
            codeError.value = R.string.verification_code_empty_error
            return false
        }
        codeError.value = null
        return true
    }

    fun done() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        if (!isCodeValid()) return

        currentCode.value?.let {
            isBusy.value = true
            authRepository.verifyCode(it, getAuthType(),
                object : IAuthenticationRepository.IOnVerifyListener {
                    override fun onSuccess() {
                        isBusy.value = false
                        prefsHelper.saveSkipTwoStepVerification(true)
                        onCodeVerified.call()
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        errorMessage.value =
                            if (error.equals(Constants.ERROR_INVALID_VERIFICATION_CODE) ||
                                error.equals(Constants.ERROR_EXPIRED_VERIFICATION_CODE)
                            ) PermanentApplication.instance.getString(
                                R.string.verification_code_invalid_error
                            ) else error
                    }
                })
        }
    }

    private fun getAuthType(): String {
        if (isSmsCodeFlow) return Constants.AUTH_TYPE_PHONE
        return Constants.AUTH_TYPE_MFA_VALIDATION
    }

    fun getDefaultArchive() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        archiveRepository.getAllArchives(object : IDataListener {
            override fun onSuccess(dataList: List<Datum>?) {
                isBusy.value = false
                if (!dataList.isNullOrEmpty()) {
                    val defaultArchiveId = prefsHelper.getDefaultArchiveId()

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
                            onLoggedIn.call()
                            return
                        }
                    }
                }
                errorMessage.value = appContext.getString(R.string.generic_error)
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                errorMessage.value = error
            }
        })
    }
}