package org.permanent.permanent.repositories

import org.permanent.permanent.models.Record
import org.permanent.permanent.models.Share
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.ShareRequestType
import org.permanent.permanent.network.models.ShareVO
import org.permanent.permanent.network.models.Shareby_urlVO

interface IShareRepository {

    // RECORD SHARE LINK
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

    fun updateShare(share: Share, listener: IResponseListener)

    fun deleteShare(share: Share, listener: IResponseListener)

    // SHARE PREVIEW
    fun checkShareLink(urlToken: String, listener: IShareByUrlListener)

    fun requestShareAccess(urlToken: String, listener: IShareListener)

    // SHARES
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