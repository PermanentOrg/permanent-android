package org.permanent.permanent.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.AuthenticationRepositoryImpl
import org.permanent.permanent.repositories.IAuthenticationRepository
import org.permanent.permanent.repositories.INotificationRepository
import org.permanent.permanent.repositories.NotificationRepositoryImpl

class SplashViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val TAG = SplashViewModel::class.java.simpleName
    private val appContext = application.applicationContext
    private val onLoggedInResponse = SingleLiveEvent<Boolean>()
    private val authRepository: IAuthenticationRepository = AuthenticationRepositoryImpl(application)

    fun getOnLoggedInResponse(): MutableLiveData<Boolean> {
        return onLoggedInResponse
    }

    fun verifyIsUserLoggedIn() {
        authRepository.verifyLoggedIn(object : IAuthenticationRepository.IOnLoggedInListener {

            override fun onResponse(isLoggedIn: Boolean) {
                onLoggedInResponse.value = isLoggedIn

                if (isLoggedIn) {
                    val notificationsRepository: INotificationRepository =
                        NotificationRepositoryImpl(appContext)

                    FirebaseMessaging.getInstance().token
                        .addOnCompleteListener(OnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                Log.e(TAG, "Fetching FCM token failed: ${task.exception}")
                                return@OnCompleteListener
                            }
                            notificationsRepository.registerDevice(task.result,
                                object : IResponseListener {

                                    override fun onSuccess(message: String?) {
                                    }

                                    override fun onFailed(error: String?) {
                                        Log.e(TAG, "Registering Device FCM token failed: $error")
                                    }
                                })
                        })
                }
            }
        })
    }
}