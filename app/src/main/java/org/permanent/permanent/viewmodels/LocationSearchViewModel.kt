package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.permanent.permanent.R
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.FileData
import org.permanent.permanent.network.models.LocnVO
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.repositories.ILocationRepository
import org.permanent.permanent.repositories.LocationRepositoryImpl

class LocationSearchViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private var appContext: Context? = application.applicationContext
    private var locationVO: LocnVO? = null
    private lateinit var fileData: FileData
    private lateinit var googleMap: GoogleMap
    private var marker: Marker? = null
    val showMessage = SingleLiveEvent<String>()
    private val isBusy = MutableLiveData(false)
    private val onLocationUpdate = MutableLiveData<FileData>()
    private var locationRepository: ILocationRepository = LocationRepositoryImpl(application)
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)

    fun setFileData(fileData: FileData) {
        this.fileData = fileData
    }

    fun setMap(googleMap: GoogleMap) {
        this.googleMap = googleMap
    }

    fun getCurrentFileData(): FileData = fileData

    fun getIsBusy(): LiveData<Boolean> = isBusy

    fun getOnLocationUpdate(): LiveData<FileData> = onLocationUpdate

    fun onLatLngSelected(latLng: LatLng) {
        updateMarker(latLng)
        requestLocation(latLng)
    }

    fun updateMarker(latLng: LatLng) {
        googleMap.apply {
            if (marker != null) marker?.position = latLng
            else marker = addMarker(MarkerOptions().position(latLng))
            moveCamera(CameraUpdateFactory.newLatLng(latLng))
            animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f))
        }
    }

    private fun requestLocation(latLng: LatLng) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        locationRepository.getLocation(latLng, object : ILocationRepository.LocationListener {

            override fun onSuccess(locnVO: LocnVO) {
                locationVO = locnVO
            }

            override fun onFailed(error: String?) {
            }
        })
    }

    fun updateRecordLocation() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        if (locationVO != null) {
            isBusy.value = true
            fileRepository.updateRecord(locationVO!!, fileData, object : IResponseListener {
                override fun onSuccess(message: String?) {
                    isBusy.value = false
                    showMessage.value = message
                    fileData.update(locationVO!!)
                    onLocationUpdate.value = fileData
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    showMessage.value = error
                }
            })
        } else {
            showMessage.value = appContext?.getString(R.string.file_location_update_error)
        }
    }
}
