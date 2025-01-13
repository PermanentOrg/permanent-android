package org.permanent.permanent.network.models

class TwoFAVO {
    var methodId: String? = null // ex: "VJ7F"
    lateinit var method: String // "email" or "sms"
    lateinit var value: String // "flaviahandrea+prmnttst0007@gmail.com" or "(917)  695 - 2195"
}