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

class SignUpRepositoryImpl(val application: Application) : ISignUpRepository {

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val prefsHelper = PreferencesHelper(sharedPreferences)
    private val networkClient: NetworkClient = NetworkClient(application)

    override fun signUp(
        fullName: String,
        email: String,
        password: String,
        listener: ISignUpRepository.IOnSignUpListener
    ) {
        networkClient.signUp(fullName, email, password).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                prefsHelper.saveCsrf(response.body()?.csrf!!)
                if (response.isSuccessful && response.body()?.isSuccessful!!) {
                    // We save these here in order to use them for the background login call
                    prefsHelper.saveEmail(email)
                    prefsHelper.savePassword(password)
                    listener.onSuccess()
                } else {
                    listener.onFailed(
                        response.body()?.Results?.get(0)?.message?.get(0)
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