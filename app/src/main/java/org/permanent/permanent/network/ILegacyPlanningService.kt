package org.permanent.permanent.network

import org.permanent.permanent.network.models.ArchiveSteward
import org.permanent.permanent.network.models.LegacyContact
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ILegacyPlanningService {

    @POST("api/v2/legacy-contact")
    fun addLegacyContact(@Body legacyContact: LegacyContact): Call<LegacyContact>

    @PUT("api/v2/legacy-contact/{legacyContactId}")
    fun editLegacyContact(@Path("legacyContactId") legacyContactId: String, @Body legacyContact: LegacyContact): Call<LegacyContact>

    @GET("api/v2/legacy-contact")
    fun getLegacyContact(): Call<List<LegacyContact>>

    @POST("api/v2/directive")
    fun addArchiveSteward(@Body archiveSteward: ArchiveSteward): Call<ArchiveSteward>

    @PUT("api/v2/directive/{directiveId}")
    fun editArchiveSteward(@Path("directiveId") directiveId: String, @Body archiveSteward: ArchiveSteward): Call<ArchiveSteward>

    @GET("api/v2/directive/archive/{archiveId}")
    fun getArchiveSteward(@Path("archiveId") archiveId: Int): Call<List<ArchiveSteward>>
}