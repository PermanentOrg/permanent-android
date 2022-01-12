package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Notification
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.repositories.INotificationRepository
import org.permanent.permanent.repositories.NotificationRepositoryImpl

class ActivityFeedViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val isBusy = MutableLiveData(false)
    private val showMessage = MutableLiveData<String>()
    private val existsNotifications = MutableLiveData(false)
    private val onNotificationsRetrieved = SingleLiveEvent<MutableList<Notification>>()
    private var notificationsRepository: INotificationRepository = NotificationRepositoryImpl(appContext)

    init {
        getNotifications()
    }

    private fun getNotifications() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        notificationsRepository.getNotifications(object : IDataListener {
            override fun onSuccess(dataList: List<Datum>?) {
                isBusy.value = false
                existsNotifications.value = !dataList.isNullOrEmpty()
                if (!dataList.isNullOrEmpty()) {
                    val notifications: MutableList<Notification> = ArrayList()

                    for (data in dataList) {
                        data.NotificationVO?.let {notifications.add(Notification(it)) }
                    }
                    onNotificationsRetrieved.value = notifications
                }
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showMessage.value = error
            }
        })
    }

    fun getExistsNotifications(): MutableLiveData<Boolean> = existsNotifications

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getShowMessage(): LiveData<String> {
        return showMessage
    }

    fun getOnNotificationsRetrieved(): LiveData<MutableList<Notification>> {
        return onNotificationsRetrieved
    }
}