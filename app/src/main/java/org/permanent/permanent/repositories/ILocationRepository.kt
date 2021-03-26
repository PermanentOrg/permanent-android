package org.permanent.permanent.repositories

import com.google.android.gms.maps.model.LatLng
import org.permanent.permanent.network.models.LocnVO

interface ILocationRepository {

    fun getLocation(latLng: LatLng, listener: LocationListener)

    interface LocationListener {
        fun onSuccess(locnVO: LocnVO)
        fun onFailed(error: String?)
    }
}