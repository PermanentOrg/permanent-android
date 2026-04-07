package org.permanent.permanent.network.models

data class ItemDTO(
    val folderId: String?,
    val recordId: String?,
    val folderLinkId: String?,
    val displayName: String?,
    val displayDate: String?,
    val size: Long?,
    val thumbUrl200: String?,
    var thumbUrl2000: String?,
    val archive: ArchiveDTO?,
)