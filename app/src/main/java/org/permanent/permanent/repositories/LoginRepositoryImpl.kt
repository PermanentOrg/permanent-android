package org.permanent.permanent.repositories

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import org.permanent.permanent.Constants
import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.models.ResponseVO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginRepositoryImpl(val application: Application) : ILoginRepository {

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
    private val networkClient: NetworkClient = NetworkClient(application)

    override fun verifyLoggedIn(
        listener: ILoginRepository.IOnLoggedInListener
    ) {
        networkClient.verifyLoggedIn().enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, retrofitResponse: Response<ResponseVO>) {
                val responseVO = retrofitResponse.body()
                saveCsrfTo(sharedPreferences, responseVO?.csrf!!)

                if(retrofitResponse.isSuccessful) {
                    responseVO.isUserLoggedIn()?.let { listener.onResponse(it) }
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
        saveEmailTo(sharedPreferences, email)
        networkClient.login(email, password).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                saveCsrfTo(sharedPreferences, response.body()?.csrf!!)
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

    override fun verify(
        code: String,
        listener: ILoginRepository.IOnVerifyListener
    ) {
        networkClient.verifyCode(
            code,
            getCsrfFrom(sharedPreferences),
            getEmailFrom(sharedPreferences)
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

    override fun forgotPassword(
        email: String,
        listener: ILoginRepository.IOnResetPasswordListener
    ) {
        TODO("Not yet implemented")
    }

    private fun saveEmailTo(preferences: SharedPreferences, email: String) {
        with(preferences.edit()) {
            putString(Constants.PREFS_SAVED_EMAIL, email)
            apply()
        }
    }

    private fun getEmailFrom(preferences: SharedPreferences): String {
        return preferences.getString(Constants.PREFS_SAVED_EMAIL, "")!!
    }

    private fun saveCsrfTo(preferences: SharedPreferences, csrf: String) {
        with(preferences.edit()) {
            putString(Constants.PREFS_SAVED_CSRF, csrf)
            apply()
        }
    }

    private fun getCsrfFrom(preferences: SharedPreferences): String {
        return preferences.getString(Constants.PREFS_SAVED_CSRF, "")!!
    }
}