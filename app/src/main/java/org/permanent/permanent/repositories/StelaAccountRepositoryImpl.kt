package org.permanent.permanent.repositories

import android.content.Context
import com.google.gson.Gson
import okhttp3.ResponseBody
import org.permanent.permanent.R
import org.permanent.permanent.models.Tags
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.ITwoFAListener
import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.models.ErrorResponse
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.network.models.TwoFAVO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StelaAccountRepositoryImpl(context: Context) : StelaAccountRepository {

    private val appContext = context.applicationContext

    override fun addRemoveTags(tags: Tags, listener: IResponseListener) {
        NetworkClient.instance().addRemoveTags(tags).enqueue(object : Callback<ResponseVO> {

            override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                if (response.isSuccessful) {
                    val responseVO = response.body()
                    if (responseVO != null) {
                        listener.onSuccess("")
                    } else {
                        listener.onFailed(appContext.getString(R.string.generic_error))
                    }
                } else {
                    try {
                        listener.onFailed(response.errorBody().toString())
                    } catch (e: Exception) {
                        listener.onFailed(e.message)
                    }
                }
            }

            override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun getTwoFAMethod(listener: ITwoFAListener) {
        NetworkClient.instance().getTwoFAMethod().enqueue(object : Callback<List<TwoFAVO>> {

            override fun onResponse(call: Call<List<TwoFAVO>>, response: Response<List<TwoFAVO>>) {
                if (response.isSuccessful) {
                    val twoFAVOList = response.body()
                    if (twoFAVOList != null) {
                        listener.onSuccess(twoFAVOList.ifEmpty { null })
                    } else {
                        listener.onFailed(appContext.getString(R.string.generic_error))
                    }
                } else {
                    try {
                        listener.onFailed(response.errorBody().toString())
                    } catch (e: Exception) {
                        listener.onFailed(e.message)
                    }
                }
            }

            override fun onFailure(call: Call<List<TwoFAVO>>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun sendEnableCode(twoFAVO: TwoFAVO, listener: IResponseListener) {
        NetworkClient.instance().sendEnableCode(twoFAVO).enqueue(object : Callback<ResponseBody> {

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseVO = response.body()
                    if (responseVO != null) {
                        listener.onSuccess("")
                    } else {
                        listener.onFailed(appContext.getString(R.string.generic_error))
                    }
                } else {
                    response.errorBody()?.let { errorBody ->
                        val errorJson = errorBody.string()
                        try {
                            val errorResponse =
                                Gson().fromJson(errorJson, ErrorResponse::class.java)
                            var errorMessage = errorResponse.error.details[0].message

                            errorMessage =
                                errorMessage.replace("value", twoFAVO.value, ignoreCase = true)

                            listener.onFailed(errorMessage)
                        } catch (e: Exception) {
                            listener.onFailed("Failed to parse error JSON")
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }

    override fun enableTwoFactor(twoFAVO: TwoFAVO, listener: IResponseListener) {
        NetworkClient.instance().enableTwoFactor(twoFAVO).enqueue(object : Callback<ResponseBody> {

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseVO = response.body()
                    if (responseVO != null) {
                        listener.onSuccess("")
                    } else {
                        listener.onFailed(appContext.getString(R.string.generic_error))
                    }
                } else {
                    listener.onFailed("The code is not valid.")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                listener.onFailed(t.message)
            }
        })
    }
}