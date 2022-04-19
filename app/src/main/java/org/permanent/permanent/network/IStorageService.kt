package org.permanent.permanent.network

import org.permanent.permanent.BuildConfig
import org.permanent.permanent.network.models.ResponseVO
import retrofit2.Call
import retrofit2.http.*

interface IStorageService {

    @Headers("Authorization: Basic ${BuildConfig.SECRET_KEY_ENCODED}")
    @FormUrlEncoded
    @POST
    fun getClientSecret(
        @Url url: String,
        @Field("amount") amount :Int,
        @Field("currency") currency: String,
    ): Call<ResponseVO>
}