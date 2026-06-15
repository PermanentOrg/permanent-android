package org.permanent.permanent.network.models

data class FolderResponse(val items: List<FolderItemDTO>?)

data class FolderItemDTO(
    val folderId: String?,
    val pendingShares: List<PendingShareDTO>?
)
