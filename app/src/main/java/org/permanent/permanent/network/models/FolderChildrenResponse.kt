package org.permanent.permanent.network.models

data class FolderChildrenResponse(
    val items: List<ItemDTO>?,
    val pagination: PaginationDTO? = null
)

data class PaginationDTO(
    val nextCursor: String? = null
)
