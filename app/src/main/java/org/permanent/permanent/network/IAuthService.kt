package org.permanent.permanent.network

import okhttp3.RequestBody
import org.permanent.permanent.Constants
import org.permanent.permanent.network.models.ResponseVO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface IAuthService {

    @POST("auth/loggedIn")
    fun verifyLoggedIn(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST(Constants.LOGIN_URL_SUFFIX)
    fun login(@Body request: RequestBody): Call<ResponseVO>

    @POST("auth/logout")
    fun logout(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("auth/sendEmailForgotPassword")
    fun forgotPassword(@Body request: RequestBody): Call<ResponseVO>

    @POST("auth/resendTextCreatedAccount")
    fun sendSMSVerificationCode(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("auth/verify")
    fun verifyCode(@Body requestBody: RequestBody): Call<ResponseVO>
}