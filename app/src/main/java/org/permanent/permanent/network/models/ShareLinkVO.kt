package org.permanent.permanent.network.models


data class ShareLinkVOResponse(
    val items: List<ShareLinkVO>
)

class ShareLinkVO {
    var Id:  String? = null
    var itemId:  String? = null
    var itemType:  String? = null
    var permissionsLevel:  String? = null
    var accessRestrictions: String? = null

    var token: String? = null

    var maxUses: Int? = null // can be 0 for unlimited uses

    var usesExpended: Int? = null
    var expirationTimestamp: String? = null // can be null for no expiration

    var createdAt: String? = null
    var updatedAt: String? = null
}