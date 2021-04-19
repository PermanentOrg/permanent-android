package org.permanent.permanent.models

enum class FCMNotificationType(private val backendString: String) {
    SHARED_ITEM("share-notification"),
    SHARED_FOLDER("box-notification");

    fun toBackendString(): String = backendString
}