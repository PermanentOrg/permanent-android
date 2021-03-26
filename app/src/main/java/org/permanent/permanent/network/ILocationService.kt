package org.permanent.permanent.network

import okhttp3.RequestBody
import org.permanent.permanent.network.models.ResponseVO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ILocationService {

    @POST("locn/geomapLatLong")
    fun getLocation(@Body requestBody: RequestBody): Call<ResponseVO>
}