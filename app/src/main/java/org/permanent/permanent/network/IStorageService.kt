package org.permanent.permanent.network

import org.permanent.permanent.BuildConfig
import org.permanent.permanent.network.models.ResponseVO
import retrofit2.Call
import retrofit2.http.*

interface IStorageService {

    @Headers("Authorization: Basic ${BuildConfig.SECRET_KEY_ENCODED}")
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