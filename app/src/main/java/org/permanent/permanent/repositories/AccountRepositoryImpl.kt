package org.permanent.permanent.repositories

import android.app.Application
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

class AccountRepositoryImpl(application: Application) : IAccountRepository {

    private val appContext = application.applicationContext
    private val prefsHelper =
        PreferencesHelper(application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))
    private val networkClient: NetworkClient = NetworkClient(application)

    override fun signUp(
        fullName: String, email: String, password: String, listener: IResponseListener
    ) {
        networkClient.signUp(fullName, email, password).enqueue(object : Callback<ResponseVO> {

            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()
                if (response.isSuccessful && responseVO?.isSuccessful!!) {
                    responseVO.csrf?.let { prefsHelper.saveCsrf(it) }
                    // We save this for the Update Phone call
                    prefsHelper.saveUserAccountId(responseVO.getAccount()?.accountId)
                    // We save these here in order to use them for the background login call
                    prefsHelper.saveUserEmail(email)
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
            networkClient.getAccount(prefsHelper.getCsrf(), accountId)
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
        networkClient.updateAccount(prefsHelper.getCsrf(), account)
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

    override fun changePassword(
        currentPassword: String, newPassword: String, retypedPassword: String,
        listener: IResponseListener
    ) {
        val accountId = prefsHelper.getUserAccountId()

        if (accountId != 0) {
            networkClient.changePassword(
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