package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import android.text.Editable
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import org.permanent.permanent.PermanentApplication
import org.permanent.permanent.R
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.AccountRepositoryImpl
import org.permanent.permanent.repositories.IAccountRepository
import org.permanent.permanent.repositories.INotificationRepository
import org.permanent.permanent.repositories.NotificationRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class DeleteAccountViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val TAG = DeleteAccountViewModel::class.java.simpleName
    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        PermanentApplication.instance.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))
    private val isBusy = MutableLiveData<Boolean>()
    private val isDeleteAccountEnabled = MutableLiveData(false)
    private val onAccountDeleted = SingleLiveEvent<Void?>()
    private val showMessage = MutableLiveData<String>()
    private val text = MutableLiveData<String>()
    private var accountRepository: IAccountRepository = AccountRepositoryImpl(application)

    fun getIsDeleteAccountEnabled(): MutableLiveData<Boolean> = isDeleteAccountEnabled

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getOnAccountDeleted(): SingleLiveEvent<Void?> = onAccountDeleted

    fun getShowMessage(): LiveData<String> = showMessage

    fun onTextChanged(inputText: Editable) {
        text.value = inputText.toString()
        isDeleteAccountEnabled.value = text.value?.trim() == "DELETE"
    }

    fun onDeleteAccountBtnClick() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        
        if(isDeleteAccountEnabled.value == false) {
            showMessage.value = PermanentApplication.instance.getString(R.string.delete_account_error)
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
                        deleteAccount()
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        Log.e(TAG, "Deleting Device FCM token failed: $error")
                        deleteAccount()
                    }
                })
            })
    }

    fun deleteAccount() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        isBusy.value = true
        accountRepository.delete(object : IResponseListener {
            override fun onSuccess(message: String?) {
                isBusy.value = false
                showMessage.value = message
                prefsHelper.saveUserLoggedIn(false)
                prefsHelper.saveDefaultArchiveId(0)
                prefsHelper.saveBiometricsLogIn(true) // Setting back to default
                onAccountDeleted.call()
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showMessage.value = error
            }
        })
    }
}