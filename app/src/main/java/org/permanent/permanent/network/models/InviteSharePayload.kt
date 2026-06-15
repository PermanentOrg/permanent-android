package org.permanent.permanent.network.models

data class InviteSharePayload(
    val relationship: String,
    val accessRole: String,
    val recordId: Int,
    val folderLinkId: Int,
    val fullName: String,
    val byArchiveId: Int,
    val email: String
)
