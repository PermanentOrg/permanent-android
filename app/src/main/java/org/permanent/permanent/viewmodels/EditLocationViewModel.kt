package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import org.permanent.permanent.R
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.LocnVO
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.repositories.ILocationRepository
import org.permanent.permanent.repositories.LocationRepositoryImpl

class EditLocationViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val placesClient: PlacesClient = Places.createClient(application)
    private var token = AutocompleteSessionToken.newInstance()
    private var locationRepository: ILocationRepository = LocationRepositoryImpl()
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)

    private var records: MutableList<Record> = mutableListOf()

    private var locationVO: LocnVO? = null
    private val _locations = MutableLiveData<List<AutocompletePrediction>>()

    private val onLocationChanged = MutableLiveData<String>()
    val locations: LiveData<List<AutocompletePrediction>> get() = _locations
    val defaultPosition = LatLng(38.8938592, -77.0969767)
    private val _selectedLocation = mutableStateOf(defaultPosition)
    val selectedLocation: State<LatLng> get() = _selectedLocation
    var searchText: MutableState<String> = mutableStateOf("")
    var isBusy: MutableState<Boolean> = mutableStateOf(false)
    var shouldClose: MutableState<Boolean> = mutableStateOf(false)
    val showMessage = mutableStateOf("")

    fun setRecords(records: ArrayList<Record>) {
        this.records.addAll(records)
        records.forEach { record ->
            record.fileData?.let {
                if( it.latitude != -1.0) {
                    _selectedLocation.value = LatLng(it.latitude, it.longitude)
                    requestLocation(_selectedLocation.value)
                    return@forEach
                }
            }
        }
    }

    fun fetchLocations(query: String) {
        val request =
            FindAutocompletePredictionsRequest.builder()
                .setTypeFilter(TypeFilter.ADDRESS)
                .setSessionToken(token)
                .setQuery(query)
                .build()
        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                _locations.value = response.autocompletePredictions
            }.addOnFailureListener { exception: Exception? ->
                if (exception is ApiException) {
                    showMessage.value = "Place not found: ${exception.statusCode}"
                }
            }
    }

    fun fetchPlace(prediction: AutocompletePrediction) {
        val request = FetchPlaceRequest
            .builder(prediction.placeId, listOf(Place.Field.LAT_LNG))
            .build()
        placesClient.fetchPlace(request)
            .addOnSuccessListener {
                _selectedLocation.value = it.place.latLng
                requestLocation(it.place.latLng)
                token = AutocompleteSessionToken.newInstance()
            }
            .addOnFailureListener { exception: Exception? ->
                if (exception is ApiException) {
                    showMessage.value = "Place not found: ${exception.statusCode}"
                }
            }
    }

    private fun requestLocation(latLng: LatLng) {
        locationRepository.getLocation(latLng, object : ILocationRepository.LocationListener {
            override fun onSuccess(locnVO: LocnVO) {
                locationVO = locnVO
                locationVO?.let {
                    searchText.value = it.getUIAddress()
                }
            }

            override fun onFailed(error: String?) {
            }
        })
    }

    fun updateRecordLocation() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        locationVO?.let {
            isBusy.value = true
            fileRepository.updateMultipleRecords(records = records, locnVO = it,
                object : IResponseListener {
                    override fun onSuccess(message: String?) {
                        updateRecords()
                        isBusy.value = false
                        shouldClose.value = true
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        error?.let {
                            showMessage.value = it
                        }
                    }
                })
        } ?: run {
            showMessage.value = appContext.getString(R.string.file_location_update_error)
        }
    }

    fun updateRecords() {
        records.forEach {
            it.fileData?.latitude = selectedLocation.value.latitude
            it.fileData?.longitude = selectedLocation.value.longitude
            val address = locationVO?.getUIAddress()
            it.fileData?.completeAddress = address
            address?.let { address ->
                onLocationChanged.value = address
            }
        }
    }

    fun getOnLocationChanged() = onLocationChanged
}