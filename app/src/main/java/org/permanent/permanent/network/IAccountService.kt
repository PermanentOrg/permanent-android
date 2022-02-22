package org.permanent.permanent.network

import okhttp3.RequestBody
import org.permanent.permanent.Constants
import org.permanent.permanent.network.models.ResponseVO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface IAccountService {

    @POST(Constants.SIGN_UP_URL_SUFFIX)
    fun signUp(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("account/get")
    fun getAccount(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("account/getsessionaccount")
    fun getSessionAccount(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("account/update")
    fun updateAccount(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("account/delete")
    fun deleteAccount(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("account/changePassword")
    fun changePassword(@Body requestBody: RequestBody): Call<ResponseVO>
}