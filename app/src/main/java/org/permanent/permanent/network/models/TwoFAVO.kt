package org.permanent.permanent.network.models

data class TwoFAVO(
    val methodId: String? = null, // Example: "VJ7F"
    val method: String,           // "email" or "sms"
    val value: String             // "flaviahandrea+prmnttst0007@gmail.com" or "(917)  695 - 2195"
)