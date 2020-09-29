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

class LoginRepositoryImpl(val application: Application) : ILoginRepository {

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val prefsHelper = PreferencesHelper(sharedPreferences)
    private val networkClient: NetworkClient = NetworkClient(application)

    override fun verifyLoggedIn(
        listener: ILoginRepository.IOnLoggedInListener
    ) {
        networkClient.verifyLoggedIn().enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, retrofitResponse: Response<ResponseVO>) {
                val responseVO = retrofitResponse.body()
                responseVO?.csrf?.let { prefsHelper.saveCsrf(it) }

                if(retrofitResponse.isSuccessful) {
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
        listener: ILoginRepository.IOnLoginListener
    ) {
        prefsHelper.saveEmail(email)
        networkClient.login(email, password).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                prefsHelper.saveCsrf(response.body()?.csrf!!)
                if(response.isSuccessful && response.body()?.isSuccessful!!) {
                    listener.onSuccess()
                } else {
                    listener.onFailed(response.body()?.Results?.get(0)?.message?.get(0)
                        ?: response.errorBody()?.toString())
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun forgotPassword(
        email: String,
        listener: ILoginRepository.IOnResetPasswordListener
    ) {
        networkClient.forgotPassword(email).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                if(response.isSuccessful && response.body()?.isSuccessful!!) {
                    listener.onSuccess()
                } else {
                    listener.onFailed(response.body()?.Results?.get(0)?.message?.get(0)
                        ?: response.errorBody()?.toString())
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun verifyCode(
        code: String,
        listener: ILoginRepository.IOnVerifyListener
    ) {
        networkClient.verifyCode(
            code,
            prefsHelper.getCsrf(),
            prefsHelper.getEmail()
        ).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                if(response.isSuccessful && response.body()?.isSuccessful!!) {
                    listener.onSuccess()
                } else {
                    listener.onFailed(response.body()?.Results?.get(0)?.message?.get(0)
                        ?: response.errorBody()?.toString())
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }
}