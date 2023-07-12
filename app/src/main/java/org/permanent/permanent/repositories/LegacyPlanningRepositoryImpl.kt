package org.permanent.permanent.repositories

import android.content.Context
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.models.ResponseVO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LegacyPlanningRepositoryImpl(val context: Context) : ILegacyPlanningRepository {

    override fun getLegacyContact(listener: IResponseListener) {
        NetworkClient.instance().getLegacyContact().enqueue(object : Callback<List<ResponseVO>> {

            override fun onResponse(
                call: Call<List<ResponseVO>>, response: Response<List<ResponseVO>>
            ) {
                listener.onSuccess("")
            }

            override fun onFailure(call: Call<List<ResponseVO>>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }
}