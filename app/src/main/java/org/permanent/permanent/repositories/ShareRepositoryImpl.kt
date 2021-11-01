package org.permanent.permanent.repositories

import android.content.Context
import org.permanent.permanent.R
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.Share
import org.permanent.permanent.models.Status
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.ShareRequestType
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.network.models.Shareby_urlVO
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShareRepositoryImpl(val context: Context) : IShareRepository {
    private val prefsHelper = PreferencesHelper(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    override fun requestShareLink(
        record: Record,
        shareRequestType: ShareRequestType,
        listener: IShareRepository.IShareByUrlListener
    ) {
        NetworkClient.instance().requestShareLink(prefsHelper.getCsrf(), record, shareRequestType)
            .enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    prefsHelper.saveCsrf(responseVO?.csrf)
                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                        listener.onSuccess(responseVO.getShareByUrlVO())
                    } else {
                        listener.onFailed(context.getString(R.string.generic_error))
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun modifyShareLink(
        shareByUrlVO: Shareby_urlVO,
        shareRequestType: ShareRequestType,
        listener: IResponseListener
    ) {
        NetworkClient.instance()
            .modifyShareLink(prefsHelper.getCsrf(), shareByUrlVO, shareRequestType)
            .enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    prefsHelper.saveCsrf(responseVO?.csrf)
                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                        listener.onSuccess(responseVO.getMessages()?.get(0))
                    } else {
                        listener.onFailed(context.getString(R.string.generic_error))
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun updateShare(share: Share, listener: IResponseListener) {
        NetworkClient.instance().updateShare(prefsHelper.getCsrf(), share)
            .enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    prefsHelper.saveCsrf(responseVO?.csrf)
                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                        val messageAction =
                            if (share.status.value == Status.PENDING)
                                context.getString(R.string.share_link_share_update_type_approved)
                            else context.getString(R.string.share_link_share_update_type_edited)
                        listener.onSuccess(
                            context.getString(
                                R.string.share_link_share_update_success,
                                messageAction
                            )
                        )
                    } else {
                        listener.onFailed(context.getString(R.string.generic_error))
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun deleteShare(share: Share, listener: IResponseListener) {
        NetworkClient.instance().deleteShare(prefsHelper.getCsrf(), share)
            .enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    prefsHelper.saveCsrf(responseVO?.csrf)
                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                        val messageAction =
                            if (share.status.value == Status.PENDING)
                                context.getString(R.string.share_link_share_update_type_denied)
                            else context.getString(R.string.share_link_share_update_type_removed)
                        listener.onSuccess(
                            context.getString(
                                R.string.share_link_share_update_success,
                                messageAction
                            )
                        )
                    } else {
                        listener.onFailed(context.getString(R.string.generic_error))
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun checkShareLink(urlToken: String, listener: IShareRepository.IShareByUrlListener) {
        NetworkClient.instance().checkShareLink(null, urlToken)
            .enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    prefsHelper.saveCsrf(responseVO?.csrf)
                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                        listener.onSuccess(responseVO.getShareByUrlVO())
                    } else {
                        listener.onFailed(responseVO?.Results?.get(0)?.message?.get(0))
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun requestShareAccess(urlToken: String, listener: IShareRepository.IShareListener) {
        NetworkClient.instance().requestShareAccess(prefsHelper.getCsrf(), urlToken)
            .enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    prefsHelper.saveCsrf(responseVO?.csrf)
                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                        listener.onSuccess(responseVO.getShareVO())
                    } else {
                        listener.onFailed(responseVO?.Results?.get(0)?.message?.get(0))
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun getShares(listener: IDataListener) {
        NetworkClient.instance().getShares(prefsHelper.getCsrf())
            .enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    prefsHelper.saveCsrf(responseVO?.csrf)
                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                        listener.onSuccess(responseVO.getData())
                    } else {
                        listener.onFailed(responseVO?.getMessages()?.get(0))
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }
}