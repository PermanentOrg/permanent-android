package org.permanent.permanent.models

import org.permanent.permanent.network.models.NotificationVO

class Notification private constructor() {
    var notificationId: Int? = null
    var message: String? = null
    var type: Type? = null
    var createdDT: String? = null

    constructor(notificationVO: NotificationVO) : this() {
        notificationId = notificationVO.notificationId
        message = notificationVO.message
        type = when {
            notificationVO.type?.contains(Type.SHARE.toBackendString()) == true -> Type.SHARE
            notificationVO.type?.contains(Type.ACCOUNT.toBackendString()) == true -> Type.ACCOUNT
            else -> Type.RELATIONSHIP
        }
        createdDT = notificationVO.createdDT?.substringBefore("T")
    }

    enum class Type(private val backendString: String) {
        SHARE("type.notification.pa"),
        RELATIONSHIP("type.notification.relationship"),
        ACCOUNT("type.notification.cleanup_bad_upload");

        fun toBackendString(): String = backendString
    }
}