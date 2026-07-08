package org.permanent.permanent.network.models

class FileVO {
    var size: Int? = null
    var format: String? = null // Can be: file.format.original, file.format.archivematica.access
    var contentType: String? = null // Can be: image/jpeg, video/mp4, application/pdf etc.
    var width: Int? = null
    var height: Int? = null
    var fileURL: String? = null
    var downloadURL: String? = null

    fun isAccessCopy(): Boolean = format == "file.format.archivematica.access"
}
