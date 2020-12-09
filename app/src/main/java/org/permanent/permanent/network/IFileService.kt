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

    @POST("folder/delete")
    fun deleteFolder(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("folder/copy")
    fun copyFolder(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("folder/move")
    fun moveFolder(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("record/postMetaBatch")
    fun postMeta(@Body requestBody: RequestBody): Call<ResponseVO>

    @Multipart
    @POST
    fun upload(
        @Url url: String,
        @Part("recordid") requestBody: RequestBody,
        @Part file: MultipartBody.Part
    ): Call<ResponseBody>

    @POST("record/get")
    fun getRecord(@Body requestBody: RequestBody): Call<ResponseVO>

    @Streaming
    @GET
    fun download(@Url url: String): Call<ResponseBody>

    @POST("record/delete")
    fun deleteRecord(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("record/copy")
    fun copyRecord(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("record/move")
    fun moveRecord(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("share/getLink")
    fun getShareLink(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("share/generateShareLink")
    fun generateShareLink(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("share/dropShareLink")
    fun deleteShareLink(@Body requestBody: RequestBody): Call<ResponseVO>
}