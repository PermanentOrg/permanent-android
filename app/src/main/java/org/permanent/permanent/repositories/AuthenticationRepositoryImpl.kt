package org.permanent.permanent.repositories

import android.app.Application
import android.content.Context
import org.permanent.permanent.models.Archive
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
                call: Call<ResponseVO>,
                retrofitResponse: Response<ResponseVO>
            ) {
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
                // We save this here for verifyCode
                prefsHelper.saveAccountEmail(email)

                if (response.isSuccessful && responseVO?.isSuccessful!!) {
                    prefsHelper.saveAccountId(responseVO.getAccount()?.accountId)
                    val archive = Archive(responseVO.getArchive())
                    prefsHelper.saveCurrentArchiveInfo(
                        archive.id,
                        archive.number,
                        archive.fullName,
                        archive.thumbURL200,
                        archive.accessRole
                    )
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
        NetworkClient.instance().logout(prefsHelper.getCsrf())
            .enqueue(object : Callback<ResponseVO> {
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
                            ?: response.errorBody()?.toString()
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
        prefsHelper.getAccountEmail()?.let {
            NetworkClient.instance().verifyCode(
                code,
                authType,
                prefsHelper.getCsrf(),
                it
            ).enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    prefsHelper.saveCsrf(responseVO?.csrf)

                    if (response.isSuccessful && responseVO?.isSuccessful == true) {
                        val account = responseVO.getAccount()
                        prefsHelper.saveAccountId(account?.accountId)
                        prefsHelper.saveDefaultArchiveId(account?.defaultArchiveId)
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
}