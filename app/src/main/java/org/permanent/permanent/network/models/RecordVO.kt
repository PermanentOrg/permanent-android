package org.permanent.permanent.network.models

class RecordVO {
    var id: Int? = null
    var displayName: String? = null
    var description: String? = null
    var displayDT: String? = null
    var createdDT: String? = null
    var updatedDT: String? = null
    var derivedDT: String? = null
    var derivedCreatedDT: String? = null
    var uploadFileName: String? = null
    var type: String? = null
    var size: Long? = null
    var dataStatus: Int? = null
    var parentFolderId: Int? = null
    var parentFolder_linkId: Int? = null
    var folder_linkId: Int? = null
    var recordId: Int? = null
    var folderId: Int? = null
    var archiveId: Int? = null
    var archiveNbr: String? = null
    var accessRole: String? = null
    var thumbURL200: String? = null
    var thumbURL2000: String? = null
    var LocnVO: LocnVO? = null
    var FileVOs: List<FileVO>? = null
    var TagVOs: List<TagVO>? = null
    var ShareVOs: List<ShareVO>? = null
    var status: String? = null

}