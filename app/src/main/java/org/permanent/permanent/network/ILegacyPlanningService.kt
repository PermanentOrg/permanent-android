package org.permanent.permanent.network

import org.permanent.permanent.network.models.ResponseVO
import retrofit2.Call
import retrofit2.http.GET

interface ILegacyPlanningService {

    @GET("api/v2/legacy-contact")
    fun getLegacyContact(): Call<List<ResponseVO>>
}