package org.permanent.permanent.repositories

import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IResponseListener

interface INotificationRepository {

    fun getNotifications(listener: IDataListener)

    fun registerDevice(token: String, listener: IResponseListener)
}