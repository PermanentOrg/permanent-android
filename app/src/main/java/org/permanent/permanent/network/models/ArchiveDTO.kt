package org.permanent.permanent.network.models

data class ArchiveDTO(
    val id: String?,
    val archiveNumber: String?,
    val name: String?,
    // Only shares[].archive carries it (folder routes); absent elsewhere.
    val thumbUrl200: String? = null
)