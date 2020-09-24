package org.permanent.permanent.network

import okhttp3.RequestBody
import org.permanent.permanent.network.models.ResponseVO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit Service to handle login
 */
interface LoginService {

    @POST("auth/loggedIn")
    fun loggedIn(@Body requestBody: RequestBody?): Call<ResponseVO>

    @POST("auth/login")
    fun login(@Body request: RequestBody): Call<ResponseVO>

    @POST("auth/verify")
    fun verify(@Body requestBody: RequestBody): Call<ResponseVO>
}