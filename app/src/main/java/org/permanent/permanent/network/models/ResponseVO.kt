package org.permanent.permanent.network.models

class ResponseVO {
    var Results: List<ResultVO>? = null
    var isSuccessful: Boolean? = false
    var actionFailKeys: List<String>? = null
    var isSystemUp: Boolean? = false
    var systemMessage: String? = null
    var sessionId: String? = null
    var csrf: String? = null
    var vaultExpirationTime: String? = null
    var createDT: String? = null
    var updatedDT: String? = null

    fun isUserLoggedIn(): Boolean? {
        return Results?.get(0)?.data?.get(0)?.SimpleVO?.value
    }
}
