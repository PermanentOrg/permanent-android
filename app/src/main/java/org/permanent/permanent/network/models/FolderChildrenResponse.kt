package org.permanent.permanent.network.models

data class FolderChildrenResponse(
    val items: List<ItemDTO>?,
    val pagination: PaginationDTO? = null
)

data class PaginationDTO(
    val nextCursor: String? = null,
    // Page count of the whole folder at the requested page size
    val totalPages: Int? = null
)
