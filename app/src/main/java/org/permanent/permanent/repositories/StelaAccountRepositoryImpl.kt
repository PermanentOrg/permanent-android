package org.permanent.permanent.repositories

import android.content.Context
import org.permanent.permanent.R
import org.permanent.permanent.models.Tags
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.ITwoFAListener
import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.network.models.TwoFAVO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StelaAccountRepositoryImpl(context: Context) : StelaAccountRepository {

    private val appContext = context.applicationContext
    override fun addRemoveTags(tags: Tags, listener: IResponseListener) {

        NetworkClient.instance().addRemoveTags(tags).enqueue(object : Callback<ResponseVO> {

            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                if (response.isSuccessful) {
                    val newLegacyContact = response.body()
                    if (newLegacyContact != null) {
                        listener.onSuccess("")
                    } else {
                        listener.onFailed(appContext.getString(R.string.generic_error))
                    }
                } else {
                    try {
                        listener.onFailed(response.errorBody().toString())
                    } catch (e: Exception) {
                        listener.onFailed(e.message)
                    }
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun getTwoFAMethod(listener: ITwoFAListener) {
        NetworkClient.instance().getTwoFAMethod().enqueue(object : Callback<List<TwoFAVO>> {

            override fun onResponse(call: Call<List<TwoFAVO>>, response: Response<List<TwoFAVO>>) {
                if (response.isSuccessful) {
                    val twoFAVOList = response.body()
                    if (twoFAVOList != null) {
                        listener.onSuccess(twoFAVOList.ifEmpty { null })
                    } else {
                        listener.onFailed(appContext.getString(R.string.generic_error))
                    }
                } else {
                    try {
                        listener.onFailed(response.errorBody().toString())
                    } catch (e: Exception) {
                        listener.onFailed(e.message)
                    }
                }
            }

            override fun onFailure(call: Call<List<TwoFAVO>>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }
}