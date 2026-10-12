package org.permanent.permanent.network.models

data class ArchivesV2Response(
    val items: List<ArchiveV2DTO>?
)

// Only the fields consumed are modeled (Moshi drops the rest). Both arrive as
// numeric strings; rootFolderId is the archive's top-level folder, whose children
// are the section roots (My Files / Public) — one /children hop from the folder
// navigation lands in (VSP-1788).
data class ArchiveV2DTO(
    val archiveNbr: String?,
    val rootFolderId: String?
)
