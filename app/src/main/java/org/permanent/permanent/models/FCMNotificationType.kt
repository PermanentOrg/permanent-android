package org.permanent.permanent.models

enum class FCMNotificationType(private val backendString: String) {
    SHARE("type.notification.share"),
    PA_RESPONSE("type.notification.pa_response_non_transfer"),
    SHARE_LINK_REQUEST("type.notification.sharelink.request");

    fun toBackendString(): String = backendString
}