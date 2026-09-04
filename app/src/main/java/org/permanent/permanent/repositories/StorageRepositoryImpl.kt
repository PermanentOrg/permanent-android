package org.permanent.permanent.repositories

import android.content.Context
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import org.permanent.permanent.R
import org.permanent.permanent.network.IPromoListener
import org.permanent.permanent.network.IStringDataListener
import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.network.models.StoragePurchaseResponse
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StorageRepositoryImpl(val context: Context) : IStorageRepository {

    private val prefsHelper =
        PreferencesHelper(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))

    // An unregistered key (defaults not loaded yet) must still mean Stela.
    private val useStelaStoragePurchase by lazy {
        val value = Firebase.remoteConfig.getValue(USE_STELA_STORAGE_PURCHASE_KEY)
        value.source == FirebaseRemoteConfig.VALUE_SOURCE_STATIC || value.asBoolean()
    }

    override fun createStoragePurchase(amountInUSD: Int, listener: IStringDataListener) {
        if (useStelaStoragePurchase) createStelaStoragePurchase(amountInUSD, listener)
        else getLegacyPaymentIntent(amountInUSD, listener)
    }

    private fun createStelaStoragePurchase(amountInUSD: Int, listener: IStringDataListener) {
        NetworkClient.instance().createStoragePurchase(amountInUSD)
            .enqueue(object : Callback<StoragePurchaseResponse> {

                override fun onResponse(
                    call: Call<StoragePurchaseResponse>,
                    response: Response<StoragePurchaseResponse>
                ) {
                    val clientSecret = response.body()?.data?.clientSecret
                    if (clientSecret != null) {
                        listener.onSuccess(clientSecret)
                    } else {
                        listener.onFailed(context.getString(R.string.generic_error))
                    }
                }

                override fun onFailure(call: Call<StoragePurchaseResponse>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    private fun getLegacyPaymentIntent(amountInUSD: Int, listener: IStringDataListener) {
        NetworkClient.instance().getPaymentIntent(
            prefsHelper.getAccountId(),
            prefsHelper.getAccountEmail(),
            prefsHelper.getAccountName(),
            false,
            amountInUSD
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

    companion object {
        private const val USE_STELA_STORAGE_PURCHASE_KEY = "use_stela_storage_purchase_android"
    }
}
