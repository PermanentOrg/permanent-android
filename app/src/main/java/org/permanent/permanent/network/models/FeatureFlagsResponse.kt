package org.permanent.permanent.network.models

data class FeatureFlagsResponse(
    val items: List<FeatureFlagDTO>?
)

data class FeatureFlagDTO(
    val name: String?
)
