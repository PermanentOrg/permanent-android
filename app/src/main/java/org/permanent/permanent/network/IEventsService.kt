package org.permanent.permanent.network

import EventsPayload
import okhttp3.RequestBody
import org.permanent.permanent.network.models.ResponseVO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface IEventsService {

    @POST("api/v2/event")
    fun sendEvent(@Body requestBody: EventsPayload): Call<ResponseVO>
}