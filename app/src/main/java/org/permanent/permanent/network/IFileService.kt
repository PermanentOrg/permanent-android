package org.permanent.permanent.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.permanent.permanent.network.models.ResponseVO
import retrofit2.Call
import retrofit2.http.*

interface IFileService {

    @POST("folder/getroot")
    fun getRoot(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("folder/navigateMin")
    fun navigateMin(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("folder/getLeanItems")
    fun getLeanItems(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("folder/post")
    fun createFolder(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("record/postMetaBatch")
    fun postMeta(@Body requestBody: RequestBody): Call<ResponseVO>

    @Multipart
    @POST
    fun upload(
        @Url url: String,
        @Part("recordid") requestBody: RequestBody,
        @Part file: MultipartBody.Part
    ): Call<ResponseBody>
}