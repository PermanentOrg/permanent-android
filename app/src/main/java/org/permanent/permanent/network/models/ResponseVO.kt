package org.permanent.permanent.network.models

import java.util.*

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
}
