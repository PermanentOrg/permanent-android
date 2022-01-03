package org.permanent.permanent.network

import okhttp3.RequestBody
import org.permanent.permanent.network.models.ResponseVO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface IProfileService {

    @POST("profile_item/getAllByArchiveNbr")
    fun getAllByArchiveNbr(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("profile_item/safeAddUpdate")
    fun safeAddUpdate(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("profile_item/delete")
    fun delete(@Body requestBody: RequestBody): Call<ResponseVO>
}