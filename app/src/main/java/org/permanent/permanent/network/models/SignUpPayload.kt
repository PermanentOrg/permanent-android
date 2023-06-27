package org.permanent.permanent.network

data class SignUpPayload(
    val agreed: Boolean,
    val createArchive: Boolean,
    val fullName: String,
    val optIn: Boolean,
    val primaryEmail: String,
    val password: String,
    val passwordVerify: String
)