package org.permanent.permanent.network.models

data class RecordResponse(val data: RecordDataDTO?)

data class RecordDataDTO(
    val recordId: String?,
    val pendingShares: List<PendingShareDTO>?
)

data class PendingShareDTO(
    val id: String?,
    val name: String?,
    val email: String?,
    val accessRole: String?
)
