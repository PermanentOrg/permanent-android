package org.permanent.permanent.network.models

// Property names match the backend JSON keys, don't rename
class RelationVO {
    var relationId: Int? = null
    var archiveId: Int? = null
    var relationArchiveId: Int? = null
    var publicDT: String? = null
    var type: String? = null
    var status: String? = null
    var ArchiveVO: ArchiveVO? = null
    var RelationArchiveVO: ArchiveVO? = null
    var createdDT: String? = null
    var updatedDT: String? = null
}
