package org.permanent.permanent.network.models

class FolderVO {
    var folderId: Int? = null
    var archiveNbr: String? = null
    var archiveId: Int? = null
    var displayName: String? = null
    var displayDT: String? = null
    var sort: String? = null
    var view: String? = null
    var type: String? = null
    var thumbURL200: String? = null
    var thumbURL500: String? = null
    var status: String? = null
    var parentFolderId: Int? = null
    var accessRole: String? = null
    var position: Int? = null
    var folder_linkId: Int? = null
    var parentFolder_linkId: Int? = null
    var ChildItemVOs: List<RecordVO>? = null
    var ShareVOs: List<ShareVO>? = null
}