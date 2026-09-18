package org.permanent.permanent.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Tags
import org.permanent.permanent.network.models.ArchivesV2Response
import org.permanent.permanent.network.models.CopyRecordV2Request
import org.permanent.permanent.network.models.FolderChildrenResponse
import org.permanent.permanent.network.models.FolderResponse
import org.permanent.permanent.network.models.FoldersResponse
import org.permanent.permanent.network.models.RecordResponse
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.network.models.ShareLinkResponse
import org.permanent.permanent.network.models.ShareLinkVO
import org.permanent.permanent.network.models.ShareLinkVOResponse
import org.permanent.permanent.network.models.TwoFAVO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface StelaAccountService {

    @Headers("Request-Version: 2")
    @GET("api/v2/records/{recordId}")
    fun getRecord(@Path("recordId") recordId: Int): Call<RecordResponse>

    // Response deliberately not decoded — success is the 2xx; the copy surfaces via refetch.
    @Headers("Request-Version: 2")
    @POST("api/v2/records/{recordId}/copies")
    fun copyRecord(
        @Path("recordId") recordId: Int,
        @Body body: CopyRecordV2Request
    ): Call<Void>

    @Headers("Request-Version: 2")
    @GET("api/v2/folder")
    fun getFolder(
        @Header("X-Permanent-Share-Token") shareToken: String?,
        @Query("folderIds[]") folderId: Int
    ): Call<FolderResponse>

    // Plural route requires pageSize; the singular alias above does not.
    @Headers("Request-Version: 2")
    @GET("api/v2/folders")
    fun getFolders(
        @Query("folderIds[]") folderIds: List<Int>,
        @Query("pageSize") pageSize: Int
    ): Call<FoldersResponse>

    @GET("api/v2/share-links")
    fun getShareLink(
        @Query("shareLinkIds[]") shareLinkIds: List<Int>? = null,
        @Query("shareTokens[]") shareTokens: List<String>? = null
    ): Call<ShareLinkVOResponse>

    @GET("api/v2/folder/{folderId}/children")
    fun getFolderChildren(
        @Header("X-Permanent-Share-Token") shareToken: String?,
        @Path("folderId") folderId: Int,
        @Query("pageSize") pageSize: Int = 99999999
    ): Call<FolderChildrenResponse>

    // The caller's archive memberships; items[].rootFolderId replaces the V1 getRoot
    // bootstrap (VSP-1788). Repeated (unbracketed) callerMembershipRole params — the
    // form the server's own nextPage emits; Retrofit renders a List @Query that way.
    @Headers("Request-Version: 2")
    @GET("api/v2/archives")
    fun getArchives(
        @Query("callerMembershipRole") roles: List<String> = ALL_MEMBERSHIP_ROLES,
        @Query("pageSize") pageSize: Int = ARCHIVES_PAGE_SIZE
    ): Call<ArchivesV2Response>

    // Bearer-token flavor for browsing the user's own archive (VSP-1778), on the
    // documented plural route (the singular form above is a deprecated alias).
    @Headers("Request-Version: 2")
    @GET("api/v2/folders/{folderId}/children")
    fun getFolderChildrenV2(
        @Path("folderId") folderId: Int,
        @Query("pageSize") pageSize: Int = MAX_CHILDREN_PAGE_SIZE
    ): Call<FolderChildrenResponse>

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

    companion object {
        // Interim page size for getFolderChildrenV2: request the whole folder in a
        // single page (cursor pagination deferred — nextCursor is non-null even on a
        // complete page, so loop termination is unreliable). Same value iOS ships.
        const val MAX_CHILDREN_PAGE_SIZE = 99999999

        // The archives search requires a query or a role; passing every role resolves
        // the selected archive whatever the caller's role on it. One page sized above
        // any realistic membership count — more archives than that falls back to the
        // V1 getRoot bootstrap. Same values iOS ships.
        val ALL_MEMBERSHIP_ROLES = AccessRole.values().map { it.lowerCase() }
        const val ARCHIVES_PAGE_SIZE = 100
    }
}