package org.permanent.permanent.repositories

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthenticationRepositoryImpl(val application: Application) : IAuthenticationRepository {

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val prefsHelper = PreferencesHelper(sharedPreferences)
    private val networkClient: NetworkClient = NetworkClient(application)

    override fun verifyLoggedIn(
        listener: IAuthenticationRepository.IOnLoggedInListener
    ) {
        networkClient.verifyLoggedIn().enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, retrofitResponse: Response<ResponseVO>) {
                val responseVO = retrofitResponse.body()
                prefsHelper.saveCsrf(responseVO?.csrf)

                if (retrofitResponse.isSuccessful) {
                    responseVO?.isUserLoggedIn()?.let { listener.onResponse(it) }
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
        email: String,
        password: String,
        listener: IAuthenticationRepository.IOnLoginListener
    ) {
        networkClient.login(email, password).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()
                prefsHelper.saveCsrf(responseVO?.csrf)
                prefsHelper.saveEmail(email)

                if (response.isSuccessful && responseVO?.isSuccessful!!) {
                    listener.onSuccess()
                } else {
                    listener.onFailed(
                        responseVO?.Results?.get(0)?.message?.get(0)
                            ?: response.errorBody()?.toString()
                    )
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun logout(listener: IAuthenticationRepository.IOnLogoutListener) {
        networkClient.logout(prefsHelper.getCsrf()).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()
                prefsHelper.saveCsrf(responseVO?.csrf)

                if (response.isSuccessful && responseVO?.isSuccessful!!) {
                    listener.onSuccess()
                } else {
                    listener.onFailed(
                        responseVO?.Results?.get(0)?.message?.get(0)
                            ?: response.errorBody()?.toString()
                    )
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun forgotPassword(
        email: String,
        listener: IAuthenticationRepository.IOnResetPasswordListener
    ) {
        networkClient.forgotPassword(email).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()
                prefsHelper.saveCsrf(responseVO?.csrf)

                if (response.isSuccessful && responseVO?.isSuccessful!!) {
                    listener.onSuccess()
                } else {
                    listener.onFailed(
                        responseVO?.Results?.get(0)?.message?.get(0)
                            ?: response.errorBody()?.toString())
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
        val email = prefsHelper.getEmail()

        if (accountId != null && email != null) {
            networkClient.sendSMSVerificationCode(
                prefsHelper.getCsrf(),
                accountId,
                email,
            ).enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    prefsHelper.saveCsrf(responseVO?.csrf)

                    if (response.isSuccessful && responseVO?.isSuccessful!!) {
                        listener.onSuccess()
                    } else {
                        listener.onFailed(
                            responseVO?.Results?.get(0)?.message?.get(0)
                                ?: response.errorBody()?.toString()
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
        code: String,
        authType: String,
        listener: IAuthenticationRepository.IOnVerifyListener
    ) {
        prefsHelper.getEmail()?.let {
            networkClient.verifyCode(
                code,
                authType,
                prefsHelper.getCsrf(),
                it
            ).enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    prefsHelper.saveCsrf(responseVO?.csrf)

                    if (response.isSuccessful && responseVO?.isSuccessful!!) {
                        listener.onSuccess()
                    } else {
                        listener.onFailed(responseVO?.Results?.get(0)?.message?.get(0)
                            ?: response.errorBody()?.toString())
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
        }
    }
}