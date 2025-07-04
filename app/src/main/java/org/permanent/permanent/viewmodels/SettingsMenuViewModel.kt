package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import org.permanent.permanent.models.Account
import org.permanent.permanent.models.AccountEventAction
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.ITwoFAListener
import org.permanent.permanent.network.models.ChecklistItem
import org.permanent.permanent.network.models.IChecklistListener
import org.permanent.permanent.network.models.TwoFAVO
import org.permanent.permanent.repositories.AccountRepositoryImpl
import org.permanent.permanent.repositories.AuthenticationRepositoryImpl
import org.permanent.permanent.repositories.EventsRepositoryImpl
import org.permanent.permanent.repositories.IAccountRepository
import org.permanent.permanent.repositories.IAuthenticationRepository
import org.permanent.permanent.repositories.IEventsRepository
import org.permanent.permanent.repositories.INotificationRepository
import org.permanent.permanent.repositories.NotificationRepositoryImpl
import org.permanent.permanent.repositories.StelaAccountRepository
import org.permanent.permanent.repositories.StelaAccountRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.myFiles.checklist.ChecklistItemType

class SettingsMenuViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val TAG = SettingsMenuViewModel::class.java.simpleName
    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    val showError = MutableLiveData<String>()
    private val isBusy = MutableLiveData(false)
    private val isTablet = prefsHelper.isTablet()
    private var spaceTotalBytes = MutableLiveData(0L)
    private var spaceUsedBytes = MutableLiveData(0L)
    private var spaceUsedPercentage = MutableLiveData(0)
    private var archiveThumb = MutableLiveData("")
    private var accountName = MutableLiveData("")
    private var accountEmail = MutableLiveData("")
    private var isTwoFAEnabled = MutableLiveData(false)
    private val errorMessage = MutableLiveData<String>()
    private val _showBottomSheet = MutableLiveData(false)
    val showBottomSheet: LiveData<Boolean> = _showBottomSheet
    val checklistItems = mutableStateOf<List<ChecklistItem>>(emptyList())
    private val onLoggedOut = SingleLiveEvent<Void?>()
    private var accountRepository: IAccountRepository = AccountRepositoryImpl(application)
    private var authRepository: IAuthenticationRepository =
        AuthenticationRepositoryImpl(application)
    private var eventsRepository: IEventsRepository = EventsRepositoryImpl(application)
    private var stelaAccountRepository: StelaAccountRepository =
        StelaAccountRepositoryImpl(application)

    fun openAccountMenuSheet() {
        _showBottomSheet.value = true
        sendEvent(AccountEventAction.OPEN_ACCOUNT_MENU)
        updateArchiveAndAccountDetails()
        updateUsedStorage()
        updateTwoFA()
        getChecklist()
    }

    fun closeAccountMenuSheet() {
        _showBottomSheet.value = false
    }

    private fun updateArchiveAndAccountDetails() {
        archiveThumb.value = prefsHelper.getCurrentArchiveThumbURL()
        accountName.value = prefsHelper.getAccountName()
        accountEmail.value = prefsHelper.getAccountEmail()
        isTwoFAEnabled.value = prefsHelper.isTwoFAEnabled()
    }

    private fun updateUsedStorage() {
        accountRepository.getAccount(object : IAccountRepository.IAccountListener {
            override fun onSuccess(account: Account) {
                val spaceTotal = account.spaceTotal
                val spaceLeft = account.spaceLeft
                if (spaceTotal != null && spaceLeft != null) {
                    spaceTotalBytes.value = spaceTotal
                    val spaceUsed = spaceTotal - spaceLeft
                    spaceUsedBytes.value = spaceUsed
                    val spaceUsedPercentageFloat = spaceUsed.toFloat() / spaceTotal.toFloat() * 100
                    spaceUsedPercentage.value = spaceUsedPercentageFloat.toInt()
                }
            }

            override fun onFailed(error: String?) {
                error?.let { showError.value = it }
            }
        })
    }

    private fun updateTwoFA() {
        stelaAccountRepository.getTwoFAMethod(object : ITwoFAListener {

            override fun onSuccess(twoFAVOList: List<TwoFAVO>?) {
                isTwoFAEnabled.value = !twoFAVOList.isNullOrEmpty()
                prefsHelper.setIsTwoFAEnabled(!twoFAVOList.isNullOrEmpty())
                prefsHelper.setTwoFAList(twoFAVOList ?: emptyList())
            }

            override fun onFailed(error: String?) {
                error?.let { showError.value = it }
            }
        })
    }

    private fun getChecklist() {
        eventsRepository.getCheckList(object : IChecklistListener {

            override fun onSuccess(checklistList: List<ChecklistItem>) {
                val updatedList = checklistList.map {
                    if (it.id == ChecklistItemType.ARCHIVE_CREATED.id) it.copy(completed = true) else it
                }
                checklistItems.value = updatedList
            }

            override fun onFailed(error: String?) {
                error?.let { showError.value = it }
            }
        })
    }

    fun deleteDeviceToken() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        isBusy.value = true
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
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

    fun sendEvent(action: AccountEventAction) {
        eventsRepository.sendEventAction(
            eventAction = action,
            accountId = prefsHelper.getAccountId(),
            data = mapOf("page" to "Account Menu")
        )
    }

    fun getIsBusy() = isBusy

    fun isTablet() = isTablet

    fun getSpaceTotal() = spaceTotalBytes

    fun getSpaceUsed() = spaceUsedBytes

    fun getSpaceUsedPercentage() = spaceUsedPercentage

    fun getArchiveThumb() = archiveThumb

    fun getAccountName() = accountName

    fun getAccountEmail() = accountEmail

    fun isTwoFAEnabled() = isTwoFAEnabled

    fun getErrorMessage(): LiveData<String> = errorMessage

    fun getOnLoggedOut(): LiveData<Void?> = onLoggedOut
}
