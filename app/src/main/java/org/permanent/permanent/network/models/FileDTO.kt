package org.permanent.permanent.network.models

// One rendition of a V2 record, told apart by format (original / converted /
// archivematica access copy); type names the file format ("type.file.image.heic").
data class FileDTO(
    val size: Long? = null,
    val format: String?,
    val type: String?,
    val fileUrl: String? = null,
    val downloadUrl: String? = null,
)
