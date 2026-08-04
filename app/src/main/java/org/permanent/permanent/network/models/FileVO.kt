package org.permanent.permanent.network.models

import org.permanent.permanent.Constants

class FileVO {
    var size: Int? = null
    var format: String? = null // Can be: file.format.original, file.format.archivematica.access
    var type: String? = null // Can be: type.file.spreadsheet.ods, type.file.pdf.pdf etc.
    var contentType: String? = null // Can be: image/jpeg, video/mp4, application/pdf etc.
    var width: Int? = null
    var height: Int? = null
    var fileURL: String? = null
    var downloadURL: String? = null

    fun isAccessCopy(): Boolean = format == Constants.FILE_FORMAT_ARCHIVEMATICA_ACCESS

    fun isPdfAccessCopy(): Boolean = type == Constants.FILE_TYPE_PDF && isAccessCopy()
}
