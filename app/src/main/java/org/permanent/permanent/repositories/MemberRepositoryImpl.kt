package org.permanent.permanent.repositories

import android.content.Context
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MemberRepositoryImpl(val context: Context): IMemberRepository {
    private val prefsHelper = PreferencesHelper(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))

    override fun getMembers(listener: IDataListener) {
        NetworkClient.instance().getMembers(prefsHelper.getCsrf(), prefsHelper.getUserArchiveNr())
            .enqueue(object : Callback<ResponseVO> {
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

    override fun addMember(email: String, accessRole: AccessRole,
                           listener: IResponseListener
    ) {
        NetworkClient.instance().addMember(prefsHelper.getCsrf(), prefsHelper.getUserArchiveNr(), email,
            accessRole).enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    prefsHelper.saveCsrf(responseVO?.csrf)
                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                        listener.onSuccess(
                            context.getString(R.string.members_member_added_successfully))
                    } else if (responseVO?.getMessages()?.get(0)
                        == Constants.ERROR_MEMBER_ALREADY_ADDED) {
                        listener.onFailed(context.getString(R.string.members_member_already_added))
                    } else listener.onFailed(responseVO?.getMessages()?.get(0))
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun updateMember(accountId: Int, email: String, accessRole: AccessRole,
                              listener: IResponseListener
    ) {
        NetworkClient.instance().updateMember(prefsHelper.getCsrf(), prefsHelper.getUserArchiveNr(),
            accountId, email, accessRole).enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    prefsHelper.saveCsrf(responseVO?.csrf)
                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                        listener.onSuccess(
                            context.getString(R.string.members_member_updated_successfully))
                    } else {
                        listener.onFailed(responseVO?.getMessages()?.get(0))
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun deleteMember(accountId: Int, email: String, listener: IResponseListener
    ) {
        NetworkClient.instance().deleteMember(prefsHelper.getCsrf(), prefsHelper.getUserArchiveNr(),
            accountId, email).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()
                prefsHelper.saveCsrf(responseVO?.csrf)
                if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                    listener.onSuccess(
                        context.getString(R.string.members_member_removed_successfully))
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