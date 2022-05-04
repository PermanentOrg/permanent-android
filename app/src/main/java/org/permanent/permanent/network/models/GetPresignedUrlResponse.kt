package org.permanent.permanent.network.models

class GetPresignedUrlResponse {
    var Results: List<Result>? = null
    var isSuccessful: Boolean? = false

    fun getDestination(): UploadDestination? {
        return Results?.get(0)?.data?.get(0)?.SimpleVO?.value
    }
}