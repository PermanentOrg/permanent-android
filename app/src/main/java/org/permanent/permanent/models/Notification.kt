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
        type = when (notificationVO.type) {
            Type.SHARE.toBackendString() -> Type.SHARE
            else -> Type.RELATIONSHIP
        }
        createdDT = notificationVO.createdDT?.substringBefore("T")
    }

    enum class Type(private val backendString: String) {
        SHARE("type.notification.share"),
        RELATIONSHIP("type.notification.relationship_accept");

//        "pa_response_non_transfer": "pa response non-transfer",
//        "cleanup_bad_upload": "Cleanup bad upload",
//        "facebook_everything_retrieval_success": "Import 'Everything' from Facebook complete",
//        "facebook_everything_retrieval_failure": "Import 'Everything' from Facebook did not complete",
//        "facebook_everything_retrieval_out_of_space": "Import 'Everything' from Facebook did not complete because your account ran out of space",
//        "facebook_permanent_retrieval_success": "Import '#permanent' from Facebook complete",
//        "facebook_permanent_retrieval_failure": "Import '#permanent' from Facebook did not complete",
//        "facebook_permanent_retrieval_out_of_space": "Import '#permanent' from Facebook did not complete because your account ran out of space",
//        "pa_access_change": "PA Access Change",
//        "pa_response": "PA Response",
//        "pa_share": "PA Share",
//        "pa_transfer": "PA Transfer",
//        "relationship_accept": "Relationship Accepted",
//        "relationship_reject": "Relationship Rejected",
//        "relationship_request": "Relationship Request",
//        "share": "Share",
//        "zip": "Zip"

        fun toBackendString(): String = backendString
    }
}