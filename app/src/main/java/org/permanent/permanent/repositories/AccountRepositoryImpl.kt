package org.permanent.permanent.repositories

import android.content.Context
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.models.Account
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AccountRepositoryImpl(context: Context) : IAccountRepository {

    private val appContext = context.applicationContext
    private val prefsHelper =
        PreferencesHelper(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))

    override fun signUp(
        fullName: String, email: String, password: String, listener: IResponseListener
    ) {
        NetworkClient.instance.signUp(fullName, email, password).enqueue(object : Callback<ResponseVO> {

            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()
                if (response.isSuccessful && responseVO?.isSuccessful!!) {
                    responseVO.csrf?.let { prefsHelper.saveCsrf(it) }
                    listener.onSuccess("")
                } else {
                    listener.onFailed(responseVO?.getMessages()?.get(0)
                        ?: response.errorBody()?.toString()
                    )
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun getAccount(listener: IAccountRepository.IAccountListener) {
        val accountId = prefsHelper.getUserAccountId()

        if (accountId != 0) {
            NetworkClient.instance.getAccount(prefsHelper.getCsrf(), accountId)
                .enqueue(object : Callback<ResponseVO> {

                    override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                        val responseVO = response.body()
                        prefsHelper.saveCsrf(responseVO?.csrf)
                        if (response.isSuccessful && responseVO?.isSuccessful!!) {
                            listener.onSuccess(Account(responseVO.getAccount()))
                        } else {
                            listener.onFailed(responseVO?.getMessages()?.get(0)
                                ?: response.errorBody()?.toString())
                        }
                    }

                    override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                        listener.onFailed(t.message)
                    }
                })
        }
    }

    override fun update(account: Account, listener: IResponseListener) {
        NetworkClient.instance.updateAccount(prefsHelper.getCsrf(), account)
            .enqueue(object : Callback<ResponseVO> {

                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    prefsHelper.saveCsrf(responseVO?.csrf)
                    if (response.isSuccessful && responseVO?.isSuccessful!!) {
                        listener.onSuccess(appContext.getString(R.string.account_update_success))
                    } else {
                        val errorMessage: String? = when (val responseMessage = responseVO?.getMessages()?.get(0)) {
                            Constants.ERROR_PHONE_INVALID -> appContext.getString(R.string.invalid_phone_error)
                            else -> responseMessage
                        }
                        listener.onFailed(errorMessage)
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun delete(listener: IResponseListener) {
        val accountId = prefsHelper.getUserAccountId()
        if (accountId != 0) {
            NetworkClient.instance.deleteAccount(prefsHelper.getCsrf(), accountId)
                .enqueue(object : Callback<ResponseVO> {

                    override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                        val responseVO = response.body()
                        prefsHelper.saveCsrf(responseVO?.csrf)
                        if (response.isSuccessful && responseVO?.isSuccessful!!) {
                            listener.onSuccess(appContext.getString(R.string.account_delete_success))
                        } else {
                            listener.onFailed(responseVO?.getMessages()?.get(0)
                                ?: response.errorBody()?.toString())
                        }
                    }

                    override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                        listener.onFailed(t.message)
                    }
                })
        }
    }

    override fun changePassword(
        currentPassword: String, newPassword: String, retypedPassword: String,
        listener: IResponseListener
    ) {
        val accountId = prefsHelper.getUserAccountId()

        if (accountId != 0) {
            NetworkClient.instance.changePassword(
                prefsHelper.getCsrf(), accountId, currentPassword, newPassword, retypedPassword,
            ).enqueue(object : Callback<ResponseVO> {

                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    prefsHelper.saveCsrf(responseVO?.csrf)
                    if (response.isSuccessful && responseVO?.isSuccessful!!) {
                        listener.onSuccess(appContext.getString(R.string.security_password_update_success))
                    } else {
                        val errorMessage: String? = when (val responseMessage = responseVO?.getMessages()?.get(0)) {
                            Constants.ERROR_PASSWORD_COMPLEXITY_LOW -> appContext.getString(R.string.security_error_low_password_complexity)
                            Constants.ERROR_PASSWORD_NO_MATCH -> appContext.getString(R.string.security_error_password_no_match)
                            Constants.ERROR_PASSWORD_OLD_INCORRECT -> appContext.getString(R.string.security_error_incorrect_old_password)
                            else -> responseMessage
                        }
                        listener.onFailed(errorMessage)
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
        }
    }
}