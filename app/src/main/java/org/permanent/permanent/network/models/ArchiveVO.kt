package org.permanent.permanent.network.models

import org.permanent.permanent.models.Archive

class ArchiveVO() {
    var ItemVOs: List<ItemVO>? = null
    var accessRole: String? = null
    var fullName: String? = null
    var archiveId: Int? = null
    var archiveNbr: String? = null
    var type: String? = null
    var thumbURL200: String? = null
    var status: String? = null

    constructor(archive: Archive) : this() {
        archiveId = archive.id
        archiveNbr = archive.number
        fullName = archive.fullName
        thumbURL200 = archive.thumbURL200
        accessRole = archive.accessRole?.backendString
        status = archive.status?.toBackendString()
    }
}