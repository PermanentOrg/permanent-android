package org.permanent.permanent.models

enum class FCMNotificationType(private val backendString: String) {
    SHARE("type.notification.share"),
    PA_RESPONSE("type.notification.pa_response_non_transfer");

    fun toBackendString(): String = backendString
}