package org.permanent.permanent.repositories

import android.content.Context
import org.json.JSONObject
import org.permanent.permanent.R
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Invitation
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IInviteListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.models.InviteVO
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.invitations.UpdateType
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InvitationRepositoryImpl(val context: Context): IInvitationRepository {
    private val prefsHelper = PreferencesHelper(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))

    override fun getInvitations(listener: IDataListener) {
        NetworkClient.instance().getInvitations().enqueue(object : Callback<ResponseVO> {

            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()
                if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                    listener.onSuccess(responseVO.getData())
                } else {
                    listener.onFailed(responseVO?.getMessages()?.get(0))
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun sendInvitation(name: String, email: String, listener: IResponseListener) {
        NetworkClient.instance().sendInvitation(name, email)
            .enqueue(object : Callback<ResponseVO> {

                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()

                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                        listener.onSuccess(context.getString(
                            R.string.invitations_update_success,
                            context.getString(R.string.invitations_update_type_sent)
                        ))
                    } else {
                        listener.onFailed(responseVO?.getMessages()?.get(0))
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun updateInvitation(inviteId: Int, type: UpdateType, listener: IResponseListener) {
        NetworkClient.instance().updateInvitation(inviteId, type)
            .enqueue(object : Callback<ResponseVO> {

                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()

                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                        val updateTypeVerb =
                            if (type == UpdateType.RESEND) context.getString(R.string.invitations_update_type_resent) else context.getString(R.string.invitations_update_type_revoked)
                        listener.onSuccess(context.getString(R.string.invitations_update_success, updateTypeVerb))
                    } else {
                        listener.onFailed(responseVO?.getMessages()?.get(0))
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun shareInvitation(
        email: String,
        fullName: String,
        accessRole: AccessRole,
        recordId: Int,
        folderLinkId: Int,
        byArchiveId: Int,
        listener: IInviteListener
    ) {
        NetworkClient.instance()
            .shareInvitation(email, fullName, accessRole, recordId, folderLinkId, byArchiveId)
            .enqueue(object : Callback<InviteVO> {

                override fun onResponse(call: Call<InviteVO>, response: Response<InviteVO>) {
                    val inviteVO = response.body()
                    if (response.isSuccessful && inviteVO?.inviteId != null) {
                        listener.onSuccess(Invitation(inviteVO))
                    } else {
                        listener.onFailed(getErrorMessageForInviteShare(response))
                    }
                }

                override fun onFailure(call: Call<InviteVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun updateInvitationReturningInvite(
        inviteId: Int, type: UpdateType, listener: IInviteListener
    ) {
        NetworkClient.instance().updateInvitation(inviteId, type)
            .enqueue(object : Callback<ResponseVO> {

                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()

                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                        listener.onSuccess(responseVO.getInviteVO()?.let { Invitation(it) })
                    } else {
                        listener.onFailed(responseVO?.getMessages()?.get(0))
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    // The v2 invite/share endpoint returns a flat error body: {"message": "...", "detail": [...]}
    private fun getErrorMessageForInviteShare(response: Response<InviteVO>): String {
        return try {
            JSONObject(response.errorBody()!!.string()).getString("message")
        } catch (e: Exception) {
            context.getString(R.string.generic_error)
        }
    }
}