package org.permanent.permanent.repositories

import android.content.Context
import org.permanent.permanent.R
import org.permanent.permanent.network.IBillingListener
import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.models.StorageGift
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BillingRepositoryImpl(val context: Context) : IBillingRepository {

    override fun send(gift: StorageGift, listener: IBillingListener) {
        NetworkClient.instance().sendGift(gift)
            .enqueue(object : Callback<StorageGift> {

                override fun onResponse(call: Call<StorageGift>, response: Response<StorageGift>) {
                    if (response.isSuccessful) {
                        val storageGift = response.body()
                        if (storageGift != null) {
                            listener.onSuccess(storageGift)
                        } else {
                            listener.onFailed(context.getString(R.string.generic_error))
                        }
                    } else {
                        listener.onFailed(context.getString(R.string.generic_error))
                    }
                }

                override fun onFailure(call: Call<StorageGift>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }
}