package org.permanent.permanent.network

import okhttp3.RequestBody
import org.permanent.permanent.network.models.ResponseVO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface IArchiveService {

    // Used in managing the archives
    @POST("archive/getAllArchives")
    fun getAllArchives(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("archive/accept")
    fun acceptArchive(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("archive/decline")
    fun declineArchive(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("archive/change")
    fun switchArchive(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("archive/post")
    fun createNewArchive(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("archive/delete")
    fun deleteArchive(@Body requestBody: RequestBody): Call<ResponseVO>

    // Used in members section
    @POST("archive/getShares")
    fun getMembers(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("archive/share")
    fun addMember(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("archive/updateShare")
    fun updateMember(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("archive/unshare")
    fun deleteMember(@Body requestBody: RequestBody): Call<ResponseVO>
}