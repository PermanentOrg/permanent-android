package org.permanent.permanent.ui.activityFeed

import org.permanent.permanent.models.Notification

interface NotificationListener {

    fun onNotificationClick(notification: Notification)
}