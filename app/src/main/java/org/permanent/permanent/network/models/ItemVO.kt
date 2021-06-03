package org.permanent.permanent.network.models

class ItemVO {
    var archiveId: Int? = null
    var archiveNbr: String? = null
    var recordId: Int? = null
    var folderId: Int? = null
    var folder_linkId: Int? = null
    var parentFolder_linkId: Int? = null
    var displayName: String? = null
    var displayDT: String? = null
    var type: String? = null
    var thumbURL500: String? = null
    var status: String? = null
    var FileVOs: List<FileVO>? = null
}