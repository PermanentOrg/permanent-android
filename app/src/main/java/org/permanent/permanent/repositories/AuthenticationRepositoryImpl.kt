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

    override fun verifyLoggedIn(
        listener: IAuthenticationRepository.IOnLoggedInListener
    ) {
        NetworkClient.instance().verifyLoggedIn().enqueue(object : Callback<ResponseVO> {
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
        NetworkClient.instance().login(email, password).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()
                prefsHelper.saveCsrf(responseVO?.csrf)
                // We save this for the background login after verifyCode
                prefsHelper.saveUserEmail(email)
                prefsHelper.saveUserPass(password)

                if (response.isSuccessful && responseVO?.isSuccessful!!) {
                    // We use this in the members section
                    prefsHelper.saveAccountFullName(responseVO.getAccount()?.fullName)
                    prefsHelper.saveArchiveFullName(responseVO.getArchive()?.fullName)
                    prefsHelper.saveArchiveThumbURL(responseVO.getArchive()?.thumbURL500)
                    prefsHelper.saveUserAccountId(responseVO.getAccount()?.accountId)
                    prefsHelper.saveUserLoggedIn(true)
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
        NetworkClient.instance().logout(prefsHelper.getCsrf()).enqueue(object : Callback<ResponseVO> {
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
        NetworkClient.instance().forgotPassword(email).enqueue(object : Callback<ResponseVO> {
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
        val accountId = prefsHelper.getUserAccountId()
        val email = prefsHelper.getUserEmail()

        if (accountId != 0 && email != null) {
            NetworkClient.instance().sendSMSVerificationCode(
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
        prefsHelper.getUserEmail()?.let {
            NetworkClient.instance().verifyCode(
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