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

class AccountRepositoryImpl(application: Application) : IAccountRepository {

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val prefsHelper = PreferencesHelper(sharedPreferences)
    private val networkClient: NetworkClient = NetworkClient(application)

    override fun signUp(
        fullName: String,
        email: String,
        password: String,
        listener: IAccountRepository.IOnSignUpListener
    ) {
        networkClient.signUp(fullName, email, password).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                val responseVO = response.body()
                if (response.isSuccessful && responseVO?.isSuccessful!!) {
                    responseVO.csrf?.let { prefsHelper.saveCsrf(it) }
                    // We save this for the Update Phone call
                    val accountId = responseVO.Results?.get(0)?.data?.get(0)?.AccountVO?.accountId
                    accountId?.let { prefsHelper.saveAccountId(it) }
                    // We save these here in order to use them for the background login call
                    prefsHelper.saveUserEmail(email)
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

    override fun update(
        phoneNumber: String,
        listener: IAccountRepository.IOnPhoneUpdatedListener
    ) {
        val accountId = prefsHelper.getAccountId()
        val email = prefsHelper.getEmail()

        if (accountId != null && email != null) {
            networkClient.update(
                prefsHelper.getCsrf(),
                accountId,
                email,
                phoneNumber
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
}