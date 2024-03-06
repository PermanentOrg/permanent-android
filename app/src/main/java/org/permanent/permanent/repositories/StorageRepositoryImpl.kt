package org.permanent.permanent.repositories

import android.content.Context
import org.permanent.permanent.network.IPromoListener
import org.permanent.permanent.network.IStringDataListener
import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.models.ResponseVO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StorageRepositoryImpl(val context: Context) : IStorageRepository {

    override fun getPaymentIntent(
        accountId: Int,
        accountEmail: String?,
        accountName: String?,
        isDonationAnonymous: Boolean?,
        donationAmount: Int,
        listener: IStringDataListener
    ) {
        NetworkClient.instance().getPaymentIntent(
            accountId, accountEmail, accountName, isDonationAnonymous, donationAmount
        ).enqueue(object : Callback<ResponseVO> {

            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()
                if (response.isSuccessful) {
                    listener.onSuccess(responseVO?.paymentIntent)
                } else {
                    listener.onFailed(responseVO?.getMessages()?.get(0))
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun redeemGiftCode(code: String, listener: IPromoListener) {
        NetworkClient.instance()
            .redeemGiftCode(code)
            .enqueue(object : Callback<ResponseVO> {

                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    val promoSizeInMB = responseVO?.getPromoVO()?.sizeInMB
                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!! && promoSizeInMB != null) {
                        listener.onSuccess(promoSizeInMB)
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