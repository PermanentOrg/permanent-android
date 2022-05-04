package org.permanent.permanent.repositories

import com.google.android.gms.maps.model.LatLng
import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.models.ResponseVO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LocationRepositoryImpl : ILocationRepository {

    override fun getLocation(
        latLng: LatLng,
        listener: ILocationRepository.LocationListener
    ) {
        NetworkClient.instance().getLocation(latLng)
            .enqueue(object : Callback<ResponseVO> {

                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
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