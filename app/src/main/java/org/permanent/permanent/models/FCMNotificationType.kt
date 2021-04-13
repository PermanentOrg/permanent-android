package org.permanent.permanent.models

enum class FCMNotificationType(private val backendString: String) {
    UPLOAD_REMINDER("upload-reminder"),
    SHARE_NOTIFICATION("share-notification");

    fun toBackendString(): String = backendString
}