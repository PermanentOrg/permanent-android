package org.permanent.permanent.network

interface IResponseListener {
    fun onSuccess(message: String?)
    fun onFailed(error: String?)
}