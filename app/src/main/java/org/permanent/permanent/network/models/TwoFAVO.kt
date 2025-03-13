package org.permanent.permanent.network.models

data class TwoFAVO(
    val methodId: String? = null, // Example: "VJ7F"
    var method: String? = null,   // "email" or "sms"
    val value: String? = null,    // "flaviahandrea+prmnttst0007@gmail.com" or "(917)  695 - 2195"
    var code: String? = null      // Example: "1234"
)