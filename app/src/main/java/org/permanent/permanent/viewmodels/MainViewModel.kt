package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.R
import org.permanent.permanent.models.Account
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.*
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.bytesToHumanReadableString

class MainViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val TAG = MainViewModel::class.java.simpleName
    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val userEmail = prefsHelper.getAccountEmail()
    private val archiveThumb = MutableLiveData<String>()
    private val archiveName = MutableLiveData<String>()
    private val spaceUsedPercentage = MutableLiveData<Int>()
    private val spaceUsedText = MutableLiveData<String>()
    private val errorMessage = MutableLiveData<String>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onManageArchives = SingleLiveEvent<Void>()
    private val onLoggedOut = SingleLiveEvent<Void>()
    val versionName = MutableLiveData(
        application.getString(
            R.string.version_text, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE.toString()
        )
    )
    private var accountRepository: IAccountRepository = AccountRepositoryImpl(application)
    private var authRepository: IAuthenticationRepository =
        AuthenticationRepositoryImpl(application)

    fun getUserEmail(): String? = userEmail

    fun getArchiveThumb(): MutableLiveData<String> = archiveThumb

    fun getArchiveName(): MutableLiveData<String> = archiveName

    fun getSpaceUsedPercentage(): MutableLiveData<Int> = spaceUsedPercentage

    fun getSpaceUsedText(): MutableLiveData<String> = spaceUsedText

    fun getErrorMessage(): LiveData<String> = errorMessage

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getOnManageArchives(): LiveData<Void> = onManageArchives

    fun getOnLoggedOut(): LiveData<Void> = onLoggedOut

    fun onManageArchivesClick() {
        onManageArchives.call()
    }

    fun updateUsedStorage() {
        accountRepository.getAccount(object : IAccountRepository.IAccountListener {
            override fun onSuccess(account: Account) {
                val spaceTotal = account.spaceTotal
                val spaceLeft = account.spaceLeft
                if (spaceTotal != null && spaceLeft != null) {
                    val spaceUsed = spaceTotal - spaceLeft
                    val spaceUsedPercentageFloat = spaceUsed.toFloat() / spaceTotal.toFloat() * 100
                    spaceUsedPercentage.value = spaceUsedPercentageFloat.toInt()
                    spaceUsedText.value = bytesToHumanReadableString(spaceUsed) + " " +
                            appContext.getString(R.string.nav_settings_header_used_suffix)
                }
            }

            override fun onFailed(error: String?) {
                errorMessage.value = error
            }
        })
    }

    fun updateCurrentArchiveHeader() {
        archiveThumb.value = prefsHelper.getCurrentArchiveThumbURL()
        archiveName.value = prefsHelper.getCurrentArchiveFullName()
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
                errorMessage.value = error
            }
        })
    }
}