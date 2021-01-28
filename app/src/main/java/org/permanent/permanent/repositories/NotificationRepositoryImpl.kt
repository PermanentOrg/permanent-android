package org.permanent.permanent.repositories

import android.content.Context
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationRepositoryImpl(val context: Context): INotificationRepository {
    private val prefsHelper = PreferencesHelper(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))
    private val networkClient: NetworkClient = NetworkClient(context)

    override fun getNotifications(listener: IDataListener) {
        networkClient.getNotifications().enqueue(object : Callback<ResponseVO> {
                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    prefsHelper.saveCsrf(responseVO?.csrf)
                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!) {
                        listener.onSuccess(responseVO.getData())
                    } else {
                        listener.onFailed(responseVO?.getMessages()?.get(0))
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }
}