package org.permanent.permanent.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.R
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.AuthenticationRepositoryImpl
import org.permanent.permanent.repositories.IAuthenticationRepository
import org.permanent.permanent.repositories.INotificationRepository
import org.permanent.permanent.repositories.NotificationRepositoryImpl

class MainViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val TAG = MainViewModel::class.java.simpleName
    private val appContext = application.applicationContext
    private val currentAccount = MutableLiveData<String>()
    private val currentSpaceUsed = MutableLiveData<Int>()
    private val errorMessage = MutableLiveData<String>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onLoggedOut = SingleLiveEvent<Void>()
    val versionName = MutableLiveData(application.getString(
        R.string.version_text, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE.toString()))
    private var authRepository: IAuthenticationRepository = AuthenticationRepositoryImpl(application)

    fun getCurrentAccount(): MutableLiveData<String> = currentAccount

    fun getCurrentSpaceUsed(): MutableLiveData<Int> = currentSpaceUsed

    fun getErrorMessage(): LiveData<String> = errorMessage

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getOnLoggedOut(): LiveData<Void> = onLoggedOut

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