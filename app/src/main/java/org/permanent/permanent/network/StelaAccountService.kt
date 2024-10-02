package org.permanent.permanent.network

import org.permanent.permanent.models.Tags
import org.permanent.permanent.network.models.ResponseVO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.PUT

interface StelaAccountService {

    @PUT("api/v2/account/tags")
    fun addRemoveTags(@Body tags: Tags): Call<ResponseVO>

//    @GET("api/v2/idpuser")
//    fun getTwoFAMethod(): Call<ResponseVO>
}