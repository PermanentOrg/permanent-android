package org.permanent.permanent.network.models

class RecordVO {
    var id: Int? = null
    var displayName: String? = null
    var displayDT: String? = null
    var uploadFileName: String? = null
    var type: String? = null
    var typeEnum: Type? = null
    var isFolder: Boolean? = null
    var dataStatus: Int? = null
    var isRecord: Boolean? = null
    var isFetching: Boolean? = null
    var parentFolderId: Int? = null
    var parentFolder_linkId: Int? = null
    var folder_linkId: Int? = null
    var recordId: Int? = null
    var folderId: Int? = null
    var archiveId: Int? = null
    var archiveNbr: String? = null
    var thumbURL200: String? = null
    var thumbURL500: String? = null
    var thumbURL1000: String? = null
    var thumbURL2000: String? = null

    fun getDate(): String? {
        return displayDT?.substringBefore("T")
    }

    enum class Type {
        Image, Folder
    }
}