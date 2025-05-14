package org.permanent.permanent.network

import EventsPayload
import org.permanent.permanent.network.models.ChecklistResponse
import org.permanent.permanent.network.models.ResponseVO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface IEventsService {

    @POST("api/v2/event")
    fun sendEvent(@Body requestBody: EventsPayload): Call<ResponseVO>

    @GET("api/v2/event/checklist")
    fun getCheckList(): Call<ChecklistResponse>
}