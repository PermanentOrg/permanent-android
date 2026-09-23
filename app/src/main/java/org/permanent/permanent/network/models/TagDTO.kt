package org.permanent.permanent.network.models

// id arrives as a JSON number; Moshi reads it into the String slot as-is.
data class TagDTO(
    val id: String?,
    val name: String?,
)
