package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.FileData
import org.permanent.permanent.network.models.LocnVO
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.repositories.ILocationRepository
import org.permanent.permanent.repositories.LocationRepositoryImpl

class LocationSearchViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private var locationVO: LocnVO? = null
    val showMessage = MutableLiveData<String>()
    private val isBusy = MutableLiveData(false)
    private var locationRepository: ILocationRepository = LocationRepositoryImpl(application)
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)

    fun getIsBusy(): LiveData<Boolean> {
        return isBusy
    }

    fun requestLocation(latLng: LatLng) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        locationRepository.getLocation(latLng, object : ILocationRepository.LocationListener {

            override fun onSuccess(locnVO: LocnVO) {
                isBusy.value = false
                locationVO = locnVO
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showMessage.value = error
            }
        })
    }

    fun updateRecordLocation(fileData: FileData) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        locationVO?.let {
            isBusy.value = true
            fileRepository.updateRecord(it, fileData, object : IResponseListener {
                override fun onSuccess(message: String?) {
                    isBusy.value = false
                    showMessage.value = message
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    showMessage.value = error
                }
            })
        }
    }
}
