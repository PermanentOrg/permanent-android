package org.permanent.permanent.network.models

class RecordVO {
    var id: Int? = null
    var displayName: String? = null
    var createdDT: String? = null
    var type: String? = null
    var typeEnum: Type? = null
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
        return createdDT?.substringBefore("T")
    }

    enum class Type {
        Image, Folder
    }
}