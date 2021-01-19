package org.permanent.permanent.repositories

import org.permanent.permanent.models.Record
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.ShareRequestType
import org.permanent.permanent.network.models.ShareVO
import org.permanent.permanent.network.models.Shareby_urlVO

interface IShareRepository {

    fun requestShareLink(
        record: Record,
        shareRequestType: ShareRequestType,
        listener: IShareByUrlListener
    )

    fun modifyShareLink(
        shareByUrlVO: Shareby_urlVO,
        shareRequestType: ShareRequestType,
        listener: IResponseListener
    )

    fun checkShareLink(urlToken: String, listener: IShareByUrlListener)

    fun requestShareAccess(urlToken: String, listener: IShareListener)

    fun getShares(listener: IDataListener)

    interface IShareByUrlListener {
        fun onSuccess(shareByUrlVO: Shareby_urlVO?)
        fun onFailed(error: String?)
    }

    interface IShareListener {
        fun onSuccess(shareVO: ShareVO?)
        fun onFailed(error: String?)
    }
}