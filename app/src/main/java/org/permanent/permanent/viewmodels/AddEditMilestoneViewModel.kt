package org.permanent.permanent.viewmodels

import android.app.Application
import android.app.DatePickerDialog
import android.content.Context
import android.text.Editable
import android.widget.DatePicker
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.permanent.permanent.R
import org.permanent.permanent.models.EventAction
import org.permanent.permanent.models.ProfileItem
import org.permanent.permanent.models.ProfileItemEventAction
import org.permanent.permanent.models.ProfileItemName
import org.permanent.permanent.network.IProfileItemListener
import org.permanent.permanent.network.models.LocnVO
import org.permanent.permanent.repositories.EventsRepositoryImpl
import org.permanent.permanent.repositories.IEventsRepository
import org.permanent.permanent.repositories.IProfileRepository
import org.permanent.permanent.repositories.ProfileRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import java.text.DecimalFormat
import java.text.NumberFormat

class AddEditMilestoneViewModel(application: Application) :
    ObservableAndroidViewModel(application), DatePickerDialog.OnDateSetListener,
    GoogleMap.OnMapClickListener, OnMapReadyCallback {
    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val isBusy = MutableLiveData(false)
    private val showMessage = SingleLiveEvent<String>()
    private val showError = SingleLiveEvent<String>()
    private val backRequest = SingleLiveEvent<Void?>()
    private val showDatePickerRequest = SingleLiveEvent<Void?>()
    private val showLocationSearchRequest = SingleLiveEvent<ProfileItem?>()
    private val title = MutableLiveData<String>()
    private val startDate = MutableLiveData<String>()
    private val endDate = MutableLiveData<String>()
    private val location = MutableLiveData<String>()
    private val description = MutableLiveData<String>()
    private val descriptionCharsNr =
        MutableLiveData(appContext.getString(R.string.edit_description_character_limit, 0))
    private var isNewLocation = false
    private var milestoneProfileItem: ProfileItem? = null
    private var profileRepository: IProfileRepository = ProfileRepositoryImpl()
    private var eventsRepository: IEventsRepository = EventsRepositoryImpl(application)
    private var isEdit: Boolean = true
    private var isStartDatePicked: Boolean = true

    fun displayProfileItem(profileItem: ProfileItem?) {
        milestoneProfileItem = profileItem
        profileItem?.string1?.let { title.value = it }
        profileItem?.day1?.let { startDate.value = it }
        profileItem?.day2?.let { endDate.value = it }
        profileItem?.locationVO?.getUIAddress()?.let { location.value = it }
        profileItem?.string2?.let { description.value = it }
    }

    fun onTitleTextChanged(text: Editable) {
        title.value = text.toString()
    }

    fun onStartDateClick() {
        isStartDatePicked = true
        showDatePickerRequest.call()
    }

    fun onEndDateClick() {
        isStartDatePicked = false
        showDatePickerRequest.call()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val f: NumberFormat = DecimalFormat("00")
        val date = "$year-${f.format(month + 1)}-${f.format(dayOfMonth)}"
        if (isStartDatePicked) startDate.value = date else endDate.value = date
    }

    fun onDescriptionTextChanged(text: Editable) {
        description.value = text.toString()
        descriptionCharsNr.value =
            appContext.getString(R.string.edit_description_character_limit, text.length)
    }

    fun onLocationTextClick() {
        showLocationSearchRequest.value = milestoneProfileItem
    }

    override fun onMapReady(googleMap: GoogleMap) {
        milestoneProfileItem?.locationVO?.let {
            val lat = it.latitude
            val long = it.longitude
            if (lat != null && long != null) {
                val latLng = LatLng(lat, long)
                googleMap.apply {
                    addMarker(MarkerOptions().position(latLng))
                    animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 9.9f))
                    setOnMapClickListener(this@AddEditMilestoneViewModel)
                }
            }
        }
    }

    override fun onMapClick(latLng: LatLng) {
        onLocationTextClick()
    }

    fun onLocationUpdated(locnVO: LocnVO) {
        if (locnVO.locnId != null) {
            location.value = locnVO.getUIAddress()
            isNewLocation = true

            milestoneProfileItem?.let {
                if (it.locationVO?.locnId != locnVO.locnId) {
                    it.locnId1 = locnVO.locnId
                    it.locationVO = locnVO
                }
            } ?: run {
                isEdit = false
                milestoneProfileItem = ProfileItem()
                milestoneProfileItem?.archiveId = prefsHelper.getCurrentArchiveId()
                milestoneProfileItem?.fieldName = ProfileItemName.MILESTONE
                milestoneProfileItem?.locnId1 = locnVO.locnId
                milestoneProfileItem?.locationVO = locnVO
            }
        }
    }

    fun onSaveInfoBtnClick() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        val titleValue = title.value?.trim()
        val startDateValue = startDate.value?.trim()
        val endDateValue = endDate.value?.trim()
        val descriptionValue = description.value?.trim()
        milestoneProfileItem?.let {
            var updateProfileItem = false
            if (!it.string1.contentEquals(titleValue)) {
                it.string1 = titleValue
                updateProfileItem = true
            }
            if (!it.day1.contentEquals(startDateValue)) {
                it.day1 = startDateValue
                updateProfileItem = true
            }
            if (!it.day2.contentEquals(endDateValue)) {
                it.day2 = endDateValue
                updateProfileItem = true
            }
            if (!it.string2.contentEquals(descriptionValue)) {
                it.string2 = descriptionValue
                updateProfileItem = true
            }
            if (updateProfileItem || isNewLocation) addUpdateProfileItem(it)

        } ?: run {
            if (titleValue?.isNotEmpty() == true || startDateValue?.isNotEmpty() == true ||
                endDateValue?.isNotEmpty() == true || descriptionValue?.isNotEmpty() == true
            ) {
                isEdit = false
                milestoneProfileItem = ProfileItem()
                milestoneProfileItem?.archiveId = prefsHelper.getCurrentArchiveId()
                milestoneProfileItem?.fieldName = ProfileItemName.MILESTONE
                milestoneProfileItem?.string1 = titleValue
                milestoneProfileItem?.day1 = startDateValue
                milestoneProfileItem?.day2 = endDateValue
                milestoneProfileItem?.string2 = descriptionValue
                addUpdateProfileItem(milestoneProfileItem!!)
            }
        }
    }

    private fun addUpdateProfileItem(profileItemToUpdate: ProfileItem) {
        isBusy.value = true
        profileRepository.safeAddUpdateProfileItems(
            listOf(profileItemToUpdate), false,
            object : IProfileItemListener {
                override fun onSuccess(profileItem: ProfileItem) {
                    sendEvent(ProfileItemEventAction.UPDATE, profileItemToUpdate.id.toString())
                    isBusy.value = false
                    showMessage.value = appContext.getString(
                        R.string.add_edit_milestone_success,
                        if (isEdit) appContext.getString(R.string.edited)
                        else appContext.getString(R.string.added)
                    )
                    backRequest.call()
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    error?.let { showError.value = it }
                }
            })
    }

    fun sendEvent(action: EventAction, entityId: String?) {
        eventsRepository.sendEventAction(
            eventAction = action,
            accountId = prefsHelper.getAccountId(),
            entityId = entityId,
            data = mapOf()
        )
    }

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getShowMessage(): LiveData<String> = showMessage
    fun getShowError(): LiveData<String> = showError

    fun getOnBackRequest(): SingleLiveEvent<Void?> = backRequest
    fun getShowDatePicker(): SingleLiveEvent<Void?> = showDatePickerRequest
    fun getShowLocationSearch(): LiveData<ProfileItem?> = showLocationSearchRequest

    fun getTitle(): LiveData<String> = title
    fun getStartDate(): LiveData<String> = startDate
    fun getEndDate(): LiveData<String> = endDate
    fun getLocation(): LiveData<String> = location
    fun getDescription(): LiveData<String> = description
    fun getDescriptionCharsNr(): LiveData<String> = descriptionCharsNr
}