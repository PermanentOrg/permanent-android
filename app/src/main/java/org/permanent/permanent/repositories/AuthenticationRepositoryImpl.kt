package org.permanent.permanent.repositories

import android.app.Application
import android.content.Context
import org.permanent.permanent.R
import org.permanent.permanent.models.Account
import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthenticationRepositoryImpl(val application: Application) : IAuthenticationRepository {

    private val prefsHelper =
        PreferencesHelper(application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))

    override fun verifyLoggedIn(
        listener: IAuthenticationRepository.IOnLoggedInListener
    ) {
        NetworkClient.instance().verifyLoggedIn().enqueue(object : Callback<ResponseVO> {
            override fun onResponse(
                call: Call<ResponseVO>, retrofitResponse: Response<ResponseVO>
            ) {
                val responseVO = retrofitResponse.body()

                if (retrofitResponse.isSuccessful) {
                    (responseVO?.getSimpleVO()?.value as Boolean?)?.let { listener.onResponse(it) }
                        ?: listener.onResponse(false)
                } else {
                    listener.onResponse(false)
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onResponse(false)
            }
        })
    }

    override fun login(
        email: String, password: String, listener: IAuthenticationRepository.IOnLoginListener
    ) {
        NetworkClient.instance().login(email, password).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()

                if (response.isSuccessful && responseVO?.isSuccessful!!) {
                    if (responseVO.getSimpleVO()?.key.equals("authToken")) {
                        prefsHelper.saveAuthToken(responseVO.getSimpleVO()?.value as String?)

                        val account = Account(responseVO.getAccountVO())
                        prefsHelper.saveAccountInfo(account.id, account.primaryEmail, password, account.fullName)
                        prefsHelper.saveDefaultArchiveId(account.defaultArchiveId)
                        listener.onSuccess()
                    } else {
                        listener.onFailed(application.getString(R.string.login_screen_missing_auth_token_error))
                    }
                } else {
                    listener.onFailed(
                        responseVO?.Results?.get(0)?.message?.get(0) ?: response.errorBody()
                            ?.toString()
                    )
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun logout(listener: IAuthenticationRepository.IOnLogoutListener) {
        NetworkClient.instance().logout().enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()

                if (response.isSuccessful && responseVO?.isSuccessful!!) {
                    prefsHelper.saveAuthToken("")
                    prefsHelper.saveUserLoggedIn(false)
                    prefsHelper.saveDefaultArchiveId(0)
                    prefsHelper.setChecklistTooltipShown(false)
                    listener.onSuccess()
                } else {
                    listener.onFailed(
                        responseVO?.Results?.get(0)?.message?.get(0) ?: response.errorBody()
                            ?.toString()
                    )
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun forgotPassword(
        email: String, listener: IAuthenticationRepository.IOnResetPasswordListener
    ) {
        NetworkClient.instance().forgotPassword(email).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()

                if (response.isSuccessful && responseVO?.isSuccessful!!) {
                    listener.onSuccess()
                } else {
                    listener.onFailed(
                        responseVO?.Results?.get(0)?.message?.get(0) ?: response.errorBody()
                            ?.toString()
                    )
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun sendSMSVerificationCode(
        listener: IAuthenticationRepository.IOnSMSCodeSentListener
    ) {
        val accountId = prefsHelper.getAccountId()
        val email = prefsHelper.getAccountEmail()

        if (accountId != 0 && email != null) {
            NetworkClient.instance().sendSMSVerificationCode(
                accountId,
                email,
            ).enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()

                    if (response.isSuccessful && responseVO?.isSuccessful!!) {
                        listener.onSuccess()
                    } else {
                        listener.onFailed(
                            responseVO?.Results?.get(0)?.message?.get(0) ?: response.errorBody()
                                ?.toString()
                        )
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
        }
    }

    override fun verifyCode(
        code: String, authType: String, listener: IAuthenticationRepository.IOnVerifyListener
    ) {
        prefsHelper.getAccountEmail()?.let {
            NetworkClient.instance().verifyCode(
                code, authType, it
            ).enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()

                    if (response.isSuccessful && responseVO?.isSuccessful == true) {
                        if (responseVO.getAuthSimpleVO()?.key.equals("authToken")) {
                            prefsHelper.saveAuthToken(responseVO.getAuthSimpleVO()?.value)
                        }
                        val account = Account(responseVO.getAccountVO())
                        prefsHelper.saveAccountInfo(account.id, account.primaryEmail, "", account.fullName)
                        prefsHelper.saveDefaultArchiveId(account.defaultArchiveId)
                        listener.onSuccess()
                    } else {
                        listener.onFailed(
                            responseVO?.Results?.get(0)?.message?.get(0) ?: response.errorBody()
                                ?.toString()
                        )
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
        }
    }
}