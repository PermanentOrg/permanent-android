package org.permanent.permanent.repositories

import android.content.Context
import org.permanent.permanent.R
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.NetworkClient
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
                prefsHelper.saveCsrf(responseVO?.csrf)
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
        NetworkClient.instance().sendInvitation(prefsHelper.getCsrf(), name, email)
            .enqueue(object : Callback<ResponseVO> {

                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    prefsHelper.saveCsrf(responseVO?.csrf)

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
        NetworkClient.instance().updateInvitation(prefsHelper.getCsrf(), inviteId, type)
            .enqueue(object : Callback<ResponseVO> {

                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    prefsHelper.saveCsrf(responseVO?.csrf)

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
}