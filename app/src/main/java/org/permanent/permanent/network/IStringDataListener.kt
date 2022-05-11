package org.permanent.permanent.network

interface IStringDataListener {
    fun onSuccess(data: String?)
    fun onFailed(error: String?)
}