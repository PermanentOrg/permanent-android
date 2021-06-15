package org.permanent.permanent.network

import org.permanent.permanent.models.Record

interface IRecordListener {
    fun onSuccess(record: Record)
    fun onFailed(error: String?)
}