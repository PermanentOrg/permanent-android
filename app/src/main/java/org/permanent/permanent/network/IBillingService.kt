package org.permanent.permanent.network

import org.permanent.permanent.network.models.StorageGift
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface IBillingService {

    @POST("api/v2/billing/gift")
    fun send(@Body gift: StorageGift): Call<StorageGift>
}