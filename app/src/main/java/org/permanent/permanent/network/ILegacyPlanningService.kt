package org.permanent.permanent.network

import com.google.android.gms.common.internal.safeparcel.SafeParcelable.Param
import org.permanent.permanent.network.models.ArchiveSteward
import org.permanent.permanent.network.models.LegacySteward
import org.permanent.permanent.network.models.ResponseVO
import retrofit2.Call
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface ILegacyPlanningService {

    @GET("api/v2/legacy-contact")
    fun getLegacyContact(): Call<List<LegacySteward>>

    @GET("api/v2/directive/archive/{archiveId}")
    fun getArchiveSteward(@Path("archiveId") archiveId: Int): Call<List<ArchiveSteward>>
}