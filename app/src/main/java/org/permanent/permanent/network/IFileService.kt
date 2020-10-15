package org.permanent.permanent.network

import okhttp3.RequestBody
import org.permanent.permanent.network.models.ResponseVO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface IFileService {

    @POST("folder/getroot")
    fun getRoot(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("folder/navigateMin")
    fun navigateMin(@Body requestBody: RequestBody?): Call<ResponseVO>

    @POST("folder/getLeanItems")
    fun getLeanItems(@Body requestBody: RequestBody?): Call<ResponseVO>
}