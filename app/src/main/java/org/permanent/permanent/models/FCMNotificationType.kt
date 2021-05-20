package org.permanent.permanent.models

enum class FCMNotificationType(private val backendString: String) {
    SHARE("type.notification.share");

    fun toBackendString(): String = backendString
}