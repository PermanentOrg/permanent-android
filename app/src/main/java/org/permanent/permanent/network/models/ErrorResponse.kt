package org.permanent.permanent.network.models

data class ErrorResponse(val error: ErrorDetail)

data class ErrorDetail(
    val _original: OriginalData,
    val details: List<ErrorItem>
)

data class OriginalData(
    val method: String,
    val value: String,
    val emailFromAuthToken: String,
    val userSubjectFromAuthToken: String
)

data class ErrorItem(
    val message: String,
    val path: List<String>,
    val type: String,
    val context: ErrorContext
)

data class ErrorContext(
    val value: String,
    val invalids: List<String>,
    val label: String,
    val key: String
)