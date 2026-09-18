package org.permanent.permanent.network

import org.permanent.permanent.network.models.StorageGift
import org.permanent.permanent.network.models.StoragePurchaseRequest
import org.permanent.permanent.network.models.StoragePurchaseResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface IBillingService {

    @POST("api/v2/billing/gift")
    fun send(@Body gift: StorageGift): Call<StorageGift>

    @POST("api/v2/storage-purchases")
    fun createStoragePurchase(@Body request: StoragePurchaseRequest): Call<StoragePurchaseResponse>
}