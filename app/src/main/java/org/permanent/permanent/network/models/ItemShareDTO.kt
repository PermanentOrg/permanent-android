package org.permanent.permanent.network.models

// A share attached to a children item (record or folder flavor share the shape).
data class ItemShareDTO(
    val id: String?,
    val accessRole: String?,
    val status: String?,
    val archive: ArchiveDTO?,
)
