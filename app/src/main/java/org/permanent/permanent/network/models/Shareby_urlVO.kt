package org.permanent.permanent.network.models

import org.permanent.permanent.models.ShareByUrl

class Shareby_urlVO() {
    var shareUrl: String? = null
    var shareby_urlId: Int? = null
    var autoApproveToggle: Int? = null // boolean (0 or 1)
    var previewToggle: Int? = null // boolean (0 or 1)
    var expiresDT: String? = null // can be null for no expiration
    var maxUses: Int? = null // can be 0 for unlimited uses
    var byAccountId: Int? = null
    var byArchiveId: Int? = null
    var urlToken: String? = null
    var FolderVO: FolderVO? = null
    var RecordVO: RecordVO? = null
    var ArchiveVO: ArchiveVO? = null
    var AccountVO: AccountVO? = null
    var ShareVO: ShareVO? = null

    constructor(shareByUrl: ShareByUrl) : this() {
        shareUrl = shareByUrl.shareUrl
        shareby_urlId = shareByUrl.shareByUrlId
        autoApproveToggle = shareByUrl.autoApproveToggle
        previewToggle = shareByUrl.previewToggle
        expiresDT = shareByUrl.expiresDT
        maxUses = shareByUrl.maxUses
        byAccountId = shareByUrl.byAccountId
        byArchiveId = shareByUrl.byArchiveId
    }
}