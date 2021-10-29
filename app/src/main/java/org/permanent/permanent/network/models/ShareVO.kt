package org.permanent.permanent.network.models

import org.permanent.permanent.models.Share

class ShareVO private constructor() {
    var shareId: Int? = null
    var folder_linkId: Int? = null
    var archiveId: Int? = null
    var ArchiveVO: ArchiveVO? = null
    var accessRole: String? = null
    var status: String? = null

    constructor(share: Share) : this() {
        shareId = share.id
        folder_linkId = share.folderLinkId
        archiveId = share.archiveId
        accessRole = share.accessRole
        status = share.status.value?.toBackendString()
    }
}