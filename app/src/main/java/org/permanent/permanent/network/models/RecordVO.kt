package org.permanent.permanent.network.models

class RecordVO {
    var dataStatus: Int? = null
    var derivedCreatedDT: String? = null
    var displayName: String? = null
    var isFetching: Boolean? = null
    var isFolder: Boolean? = null
    var isRecord: Boolean? = null
    var parentFolderId: Int? = null
    var parentFolder_linkId: Int? = null
    var uploadFileName: String? = null
    var FileVOs: List<FileVO>? = null
    var type: String? = null
    var folder_linkId: Int? = null
    var recordId: Int? = null
    var folderId: Int? = null
    var archiveId: Int? = null
    var archiveNbr: String? = null
    var thumbURL200: String? = null
    var thumbURL500: String? = null
    var thumbURL1000: String? = null
    var thumbURL2000: String? = null
}