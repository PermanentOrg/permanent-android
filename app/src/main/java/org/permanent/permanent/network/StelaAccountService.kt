package org.permanent.permanent.network

import okhttp3.ResponseBody
import org.permanent.permanent.models.Tags
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.network.models.TwoFAVO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface StelaAccountService {

    @PUT("api/v2/account/tags")
    fun addRemoveTags(@Body tags: Tags): Call<ResponseVO>

    @GET("api/v2/idpuser")
    fun getTwoFAMethod(): Call<List<TwoFAVO>>

    @POST("api/v2/idpuser/send-enable-code")
    fun sendEnableCode(@Body twoFAVO: TwoFAVO): Call<ResponseBody>
}