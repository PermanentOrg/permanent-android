package org.permanent.permanent.repositories

import org.permanent.permanent.network.IDataListener

interface INotificationRepository {

    fun getNotifications(listener: IDataListener)
}