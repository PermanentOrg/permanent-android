package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
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
    private var fileData: FileData? = null
    val showMessage = SingleLiveEvent<String>()
    private val isBusy = MutableLiveData(false)
    private val onLocationUpdate = MutableLiveData<FileData>()
    private var locationRepository: ILocationRepository = LocationRepositoryImpl(application)
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)

    fun getCurrentFileData(): FileData? = fileData

    fun getIsBusy(): LiveData<Boolean> = isBusy

    fun getOnLocationUpdate(): LiveData<FileData> = onLocationUpdate

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
            }
        })
    }

    fun updateRecordLocation(fileData: FileData) {
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
                    this@LocationSearchViewModel.fileData = fileData
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
