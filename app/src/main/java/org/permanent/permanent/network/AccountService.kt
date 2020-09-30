package org.permanent.permanent.network

import okhttp3.RequestBody
import org.permanent.permanent.network.models.ResponseVO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AccountService {

    @POST("account/post")
    fun signUp(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("account/update")
    fun updatePhone(@Body requestBody: RequestBody): Call<ResponseVO>
}