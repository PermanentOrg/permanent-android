package org.permanent.permanent.repositories

import android.content.Context
import org.permanent.permanent.models.Tag
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TagRepositoryImpl(val context: Context) : ITagRepository {
    private val prefsHelper = PreferencesHelper(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    override fun getTagsByArchive(archiveId: Int, listener: IDataListener) {
        NetworkClient.instance().getTagsByArchive(prefsHelper.getCsrf(), archiveId)
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

    override fun createOrLinkTags(tags: List<Tag>, recordId: Int, listener: IResponseListener) {
        NetworkClient.instance().createOrLinkTag(prefsHelper.getCsrf(), tags, recordId)
            .enqueue(object : Callback<ResponseVO> {

                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    prefsHelper.saveCsrf(responseVO?.csrf)
                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                        listener.onSuccess("")
                    } else {
                        listener.onFailed(responseVO?.getMessages()?.get(0))
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun unlinkTags(tags: List<Tag>, recordId: Int, listener: IResponseListener) {
        NetworkClient.instance().unlinkTags(prefsHelper.getCsrf(), tags, recordId)
            .enqueue(object : Callback<ResponseVO> {

                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    prefsHelper.saveCsrf(responseVO?.csrf)
                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                        listener.onSuccess("")
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