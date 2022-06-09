package org.permanent.permanent.network

import org.permanent.permanent.network.models.ResponseVO
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Url

interface IStorageService {

    @FormUrlEncoded
    @POST
    fun getPaymentIntent(
        @Url url: String,
        @Field("accountId") accountId :Int,
        @Field("email") email: String?,
        @Field("name") name: String?,
        @Field("anonymous") isAnonymous: Boolean?,
        @Field("amount") amount :Int,
    ): Call<ResponseVO>
}