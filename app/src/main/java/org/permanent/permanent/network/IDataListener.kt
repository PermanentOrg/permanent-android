package org.permanent.permanent.network

import org.permanent.permanent.network.models.Datum

interface IDataListener {
    fun onSuccess(dataList: List<Datum>?)
    fun onFailed(error: String?)
}