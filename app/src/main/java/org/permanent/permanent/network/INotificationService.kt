package org.permanent.permanent.network

import okhttp3.RequestBody
import org.permanent.permanent.network.models.ResponseVO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface INotificationService {

    @POST("notification/getMyNotifications")
    fun getNotifications(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("notification/update")
    fun updateNotification(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("device/registerDevice")
    fun registerDevice(@Body requestBody: RequestBody): Call<ResponseVO>
}