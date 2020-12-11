package org.permanent.permanent.network.models

import org.permanent.permanent.models.ShareByUrl

class Shareby_urlVO {
    var shareUrl: String? = null
    var shareby_urlId: Int? = null
    var autoApproveToggle: Int? = null // boolean (0 or 1)
    var previewToggle: Int? = null // boolean (0 or 1)
    var expiresDT: String? = null // can be null for no expiration
    var maxUses: Int? = null // can be 0 for unlimited uses
    var byAccountId: Int? = null
    var byArchiveId: Int? = null

    fun getShareByUrl(): ShareByUrl {
        val shareByUrl = ShareByUrl()
        shareByUrl.shareUrl = shareUrl
        shareByUrl.shareByUrlId = shareby_urlId
        shareByUrl.autoApproveToggle = autoApproveToggle
        shareByUrl.previewToggle = previewToggle
        shareByUrl.expiresDT = expiresDT
        shareByUrl.maxUses = maxUses
        shareByUrl.byAccountId = byAccountId
        shareByUrl.byArchiveId = byArchiveId

        return shareByUrl
    }
}