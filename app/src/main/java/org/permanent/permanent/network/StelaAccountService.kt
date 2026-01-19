package org.permanent.permanent.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.permanent.permanent.models.Tags
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.network.models.ShareLinkResponse
import org.permanent.permanent.network.models.ShareLinkVO
import org.permanent.permanent.network.models.ShareLinkVOResponse
import org.permanent.permanent.network.models.TwoFAVO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface StelaAccountService {

    @GET("api/v2/share-links")
    fun getShareLink(@Query("shareLinkIds[]") shareLinkIds: List<Int>): Call<ShareLinkVOResponse>

    @POST("api/v2/share-links")
    fun generateShareLink(@Body shareLink: ShareLinkVO): Call<ShareLinkResponse>

    @PATCH("api/v2/share-links/{shareLinkId}")
    fun updateShareLink(@Path("shareLinkId") shareLinkId: String?,  @Body body: RequestBody): Call<ResponseVO>

    @DELETE("api/v2/share-links/{shareLinkId}")
    fun deleteShareLink(@Path("shareLinkId") shareLinkId: String?): Call<ResponseVO>

    @PUT("api/v2/account/tags")
    fun addRemoveTags(@Body tags: Tags): Call<ResponseVO>

    @GET("api/v2/idpuser")
    fun getTwoFAMethod(): Call<List<TwoFAVO>>

    @POST("api/v2/idpuser/send-enable-code")
    fun sendEnableCode(@Body twoFAVO: TwoFAVO): Call<ResponseBody>

    @POST("api/v2/idpuser/enable-two-factor")
    fun enableTwoFactor(@Body twoFAVO: TwoFAVO): Call<ResponseBody>

    @POST("api/v2/idpuser/send-disable-code")
    fun sendDisableCode(@Body twoFAVO: TwoFAVO): Call<ResponseBody>

    @POST("api/v2/idpuser/disable-two-factor")
    fun disableTwoFactor(@Body twoFAVO: TwoFAVO): Call<ResponseBody>
}