package org.permanent.permanent.repositories

import org.permanent.permanent.network.models.ShareVO

sealed class RequestShareAccessResult {
    data class Success(val shareVO: ShareVO?) : RequestShareAccessResult()
    object AlreadyExists : RequestShareAccessResult()
    data class Error(val message: String?) : RequestShareAccessResult()
}
