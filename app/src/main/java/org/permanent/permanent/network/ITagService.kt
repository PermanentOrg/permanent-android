package org.permanent.permanent.network

import okhttp3.RequestBody
import org.permanent.permanent.network.models.ResponseVO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ITagService {

    @POST("tag/getTagsByArchive")
    fun getTagsByArchive(@Body requestBody: RequestBody): Call<ResponseVO>
}