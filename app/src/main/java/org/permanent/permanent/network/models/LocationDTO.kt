package org.permanent.permanent.network.models

data class LocationDTO(
    val id: String?,
    val streetNumber: String?,
    val streetName: String?,
    val locality: String?,
    val state: String?,
    val countryCode: String?,
    val latitude: Double?,
    val longitude: Double?,
)
