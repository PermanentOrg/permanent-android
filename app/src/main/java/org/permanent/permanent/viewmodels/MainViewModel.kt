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
import org.permanent.permanent.models.Archive
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.repositories.*
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.bytesToCustomHumanReadableString

class MainViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val TAG = MainViewModel::class.java.simpleName
    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val userEmail = MutableLiveData<String>()
    private val archiveThumb = MutableLiveData<String>()
    private val archiveName = MutableLiveData<String>()
    private val spaceUsedPercentage = MutableLiveData<Int>()
    private val spaceUsedText = MutableLiveData<String>()
    private val errorMessage = MutableLiveData<String>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onViewProfile = SingleLiveEvent<Void>()
    private val onArchiveSwitched = SingleLiveEvent<Void>()
    private val onLoggedOut = SingleLiveEvent<Void>()
    val versionName = MutableLiveData(
        application.getString(
            R.string.version_text, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE.toString()
        )
    )
    private var accountRepository: IAccountRepository = AccountRepositoryImpl(application)
    private var authRepository: IAuthenticationRepository =
        AuthenticationRepositoryImpl(application)
    private var archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)

    fun switchCurrentArchiveTo(archiveNr: String?) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        archiveNr?.let { archiveNumber ->
            isBusy.value = true
            archiveRepository.switchToArchive(archiveNumber, object : IDataListener {

                override fun onSuccess(dataList: List<Datum>?) {
                    isBusy.value = false
                    if (!dataList.isNullOrEmpty()) {
                        val archive = Archive(dataList[0].ArchiveVO)
                        prefsHelper.saveCurrentArchiveInfo(
                            archive.id,
                            archive.number,
                            archive.type,
                            archive.fullName,
                            archive.thumbURL200,
                            archive.accessRole
                        )
                    }
                    onArchiveSwitched.call()
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    error?.let { errorMessage.value = it }
                }
            })
        }
    }

    fun onViewProfileClick() {
        onViewProfile.call()
    }

    fun updateUsedStorage() {
        accountRepository.getAccount(object : IAccountRepository.IAccountListener {
            override fun onSuccess(account: Account) {
                account.primaryEmail?.let { userEmail.value = it }
                val spaceTotal = account.spaceTotal
                val spaceLeft = account.spaceLeft
                if (spaceTotal != null && spaceLeft != null) {
                    val spaceUsed = spaceTotal - spaceLeft
                    val spaceUsedPercentageFloat = spaceUsed.toFloat() / spaceTotal.toFloat() * 100
                    spaceUsedPercentage.value = spaceUsedPercentageFloat.toInt()
                    spaceUsedText.value = appContext.getString(
                        R.string.nav_settings_header_storage_text,
                        bytesToCustomHumanReadableString(spaceUsed, true),
                        bytesToCustomHumanReadableString(spaceTotal, false)
                    )
                }
            }

            override fun onFailed(error: String?) {
                error?.let { errorMessage.value = it }
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
                        Log.e(TAG, "Deleting Device FCM token failed: $error")
                        logout()
                    }
                })
            })
    }

    fun logout() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        FirebaseMessaging.getInstance().deleteToken()
        authRepository.logout(object : IAuthenticationRepository.IOnLogoutListener {
            override fun onSuccess() {
                isBusy.value = false
                onLoggedOut.call()
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                error?.let { errorMessage.value = it }
            }
        })
    }

    fun getUserEmail(): MutableLiveData<String> = userEmail

    fun getArchiveThumb(): MutableLiveData<String> = archiveThumb

    fun getArchiveName(): MutableLiveData<String> = archiveName

    fun getSpaceUsedPercentage(): MutableLiveData<Int> = spaceUsedPercentage

    fun getSpaceUsedText(): MutableLiveData<String> = spaceUsedText

    fun getErrorMessage(): LiveData<String> = errorMessage

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getOnArchiveSwitched(): LiveData<Void> = onArchiveSwitched

    fun getOnViewProfile(): LiveData<Void> = onViewProfile

    fun getOnLoggedOut(): LiveData<Void> = onLoggedOut
}