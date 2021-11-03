package org.permanent.permanent.network

import okhttp3.RequestBody
import org.permanent.permanent.network.models.ResponseVO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface IShareService {

    // RECORD SHARE LINK
    @POST("share/getLink")
    fun getShareLink(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("share/generateShareLink")
    fun generateShareLink(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("share/updateShareLink")
    fun updateShareLink(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("share/dropShareLink")
    fun deleteShareLink(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("share/upsert")
    fun updateShare(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("share/delete")
    fun deleteShare(@Body requestBody: RequestBody): Call<ResponseVO>

    // SHARE PREVIEW
    @POST("share/checkShareLink")
    fun checkShareLink(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("share/requestShareAccess")
    fun requestShareAccess(@Body requestBody: RequestBody): Call<ResponseVO>

    // SHARES
    @POST("share/getShares")
    fun getShares(@Body requestBody: RequestBody): Call<ResponseVO>
}