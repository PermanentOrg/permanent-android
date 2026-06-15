package org.permanent.permanent.network

import okhttp3.RequestBody
import org.permanent.permanent.network.models.InviteVO
import org.permanent.permanent.network.models.ResponseVO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IInvitationService {

    @POST("invite/getMyInvites")
    fun getInvitations(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("invite/inviteSend")
    fun sendInvitation(@Body requestBody: RequestBody): Call<ResponseVO>

    @Headers("Request-Version: 2")
    @POST("invite/share")
    fun shareInvitation(@Body requestBody: RequestBody): Call<InviteVO>

    @POST("invite/inviteResend")
    fun resendInvitation(@Body requestBody: RequestBody): Call<ResponseVO>

    @POST("invite/revoke")
    fun revokeInvitation(@Body requestBody: RequestBody): Call<ResponseVO>
}