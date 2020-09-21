package org.permanent.permanent.repositories

import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.models.ResponseVO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginRepositoryImpl : ILoginRepository {

    override fun login(
        email: String,
        password: String,
        listener: ILoginRepository.IOnLoginListener
    ) {
        NetworkClient().login(email, password).enqueue(object : Callback<ResponseVO> {
            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                if(response.isSuccessful && response.body()!!.isSuccessful) listener.onSuccess()
                else listener.onFailed(response.errorBody()?.toString()
                    ?: response.body()?.Results?.get(0)?.message?.get(0))
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
}