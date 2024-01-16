package org.permanent.permanent.network

interface IPromoListener {
    fun onSuccess(promoSizeInMB: Int)
    fun onFailed(error: String?)
}