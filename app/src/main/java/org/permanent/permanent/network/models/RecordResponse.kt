package org.permanent.permanent.network.models

// GET /v2/records/{id}: a record is the children record-item shape plus detail fields.
// A miss or an unauthorized read answers 200 with no data key.
data class RecordResponse(val data: ItemDTO?)

data class PendingShareDTO(
    val id: String?,
    val name: String?,
    val email: String?,
    val accessRole: String?
)
