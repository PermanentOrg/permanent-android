package org.permanent.permanent.repositories

import android.content.Context
import org.permanent.permanent.R
import org.permanent.permanent.models.Record
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

class ShareRepositoryImpl(val context: Context): IShareRepository {
    private val prefsHelper = PreferencesHelper(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))
    private val networkClient: NetworkClient = NetworkClient(context)

    override fun requestShareLink(
        record: Record,
        shareRequestType: ShareRequestType,
        listener: IShareRepository.IShareByUrlListener
    ) {
        networkClient.requestShareLink(prefsHelper.getCsrf(), record, shareRequestType)
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
        networkClient.modifyShareLink(prefsHelper.getCsrf(), shareByUrlVO, shareRequestType)
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

    override fun checkShareLink(urlToken: String, listener: IShareRepository.IShareByUrlListener) {
        networkClient.checkShareLink(null, urlToken)
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
        networkClient.requestShareAccess(prefsHelper.getCsrf(), urlToken)
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
        networkClient.getShares(prefsHelper.getCsrf()).enqueue(object : Callback<ResponseVO> {
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