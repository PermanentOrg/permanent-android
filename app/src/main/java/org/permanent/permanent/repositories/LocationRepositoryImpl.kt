package org.permanent.permanent.repositories

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LocationRepositoryImpl(val context: Context): ILocationRepository {

    private val prefsHelper = PreferencesHelper(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))

    override fun getLocation(
        latLng: LatLng,
        listener: ILocationRepository.LocationListener
    ) {
        NetworkClient.instance().getLocation(prefsHelper.getCsrf(), latLng)
            .enqueue(object : Callback<ResponseVO> {

                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    prefsHelper.saveCsrf(responseVO?.csrf)
                    if (responseVO?.isSuccessful != null && responseVO.isSuccessful!!
                        && responseVO.getLocationVO() != null) {
                        listener.onSuccess(responseVO.getLocationVO()!!)
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