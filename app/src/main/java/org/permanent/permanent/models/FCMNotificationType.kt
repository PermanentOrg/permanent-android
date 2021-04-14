package org.permanent.permanent.models

enum class FCMNotificationType(private val backendString: String) {
    SHARE_NOTIFICATION("share-notification");

    fun toBackendString(): String = backendString
}