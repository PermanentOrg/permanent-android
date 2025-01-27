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
import org.permanent.permanent.models.ArchiveType
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

class EditArchiveInformationViewModel(application: Application) :
    ObservableAndroidViewModel(application), DatePickerDialog.OnDateSetListener,
    GoogleMap.OnMapClickListener, OnMapReadyCallback {
    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val isBusy = MutableLiveData(false)
    private val showMessage = MutableLiveData<String>()
    private val showError = MutableLiveData<String>()

    private val currentArchiveType = prefsHelper.getCurrentArchiveType()
    private val nameLabel = MutableLiveData(
        when (currentArchiveType) {
            ArchiveType.FAMILY -> application.getString(R.string.public_profile_family_name_label)
            ArchiveType.ORGANIZATION -> application.getString(R.string.public_profile_organization_name_label)
            else -> application.getString(R.string.public_profile_full_name_hint)
        }
    )
    private val aliasesLabel = MutableLiveData(
        when (currentArchiveType) {
            ArchiveType.FAMILY -> application.getString(R.string.public_profile_family_aliases_label)
            ArchiveType.ORGANIZATION -> application.getString(R.string.public_profile_organization_aliases_label)
            else -> application.getString(R.string.public_profile_person_aliases_label)
        }
    )
    private val dateLabel = MutableLiveData(
        when (currentArchiveType) {
            ArchiveType.FAMILY, ArchiveType.ORGANIZATION -> application.getString(R.string.public_profile_family_and_organization_date_label)
            else -> application.getString(R.string.public_profile_person_date_label)
        }
    )
    private val locationLabel = MutableLiveData(
        when (currentArchiveType) {
            ArchiveType.FAMILY, ArchiveType.ORGANIZATION -> application.getString(R.string.public_profile_family_and_organization_location_label)
            else -> application.getString(R.string.public_profile_person_location_label)
        }
    )
    private val nameCharsNr =
        MutableLiveData(appContext.getString(R.string.edit_archive_information_character_limit, 0))
    private val aliasesCharsNr =
        MutableLiveData(appContext.getString(R.string.edit_archive_information_character_limit, 0))
    private val name = MutableLiveData("")
    private val aliases = MutableLiveData("")
    private val gender = MutableLiveData("")
    private val date = MutableLiveData("")
    private val location = MutableLiveData("")
    private var isNewLocation = false
    private val showDatePickerRequest = SingleLiveEvent<Void?>()
    private val showLocationSearchRequest = SingleLiveEvent<ProfileItem?>()
    private var nameAndAliasesProfileItem: ProfileItem? = null
    private var genderProfileItem: ProfileItem? = null
    private var dateAndLocationProfileItem: ProfileItem? = null
    private var profileRepository: IProfileRepository = ProfileRepositoryImpl()
    private var eventsRepository: IEventsRepository = EventsRepositoryImpl(application)

    fun displayProfileItems(profileItems: MutableList<ProfileItem>) {
        for (profileItem in profileItems) {
            when (profileItem.fieldName) {
                ProfileItemName.BASIC -> {
                    nameAndAliasesProfileItem = profileItem
                    profileItem.string2?.let { name.value = it }
                    profileItem.string3?.let { aliases.value = it }
                }

                ProfileItemName.GENDER -> {
                    genderProfileItem = profileItem
                    profileItem.string1?.let { gender.value = it }
                }

                ProfileItemName.BIRTH_INFO, ProfileItemName.ESTABLISHED_INFO -> {
                    dateAndLocationProfileItem = profileItem
                    profileItem.day1?.let { date.value = it }
                    profileItem.locationVO?.getUIAddress()?.let { location.value = it }
                }

                else -> {}
            }
        }
    }

    fun onNameTextChanged(text: Editable) {
        name.value = text.toString()
        nameCharsNr.value =
            appContext.getString(R.string.edit_archive_information_character_limit, text.length)
    }

    fun onAliasesTextChanged(text: Editable) {
        aliases.value = text.toString()
        aliasesCharsNr.value =
            appContext.getString(R.string.edit_archive_information_character_limit, text.length)
    }

    fun onGenderTextChanged(text: Editable) {
        gender.value = text.toString()
    }

    fun onDateClick() {
        showDatePickerRequest.call()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val f: NumberFormat = DecimalFormat("00")
        date.value = "$year-${f.format(month + 1)}-${f.format(dayOfMonth)}"
    }

    fun onLocationTextClick() {
        showLocationSearchRequest.value = dateAndLocationProfileItem
    }

    override fun onMapReady(googleMap: GoogleMap) {
        dateAndLocationProfileItem?.locationVO?.let {
            val lat = it.latitude
            val long = it.longitude
            if (lat != null && long != null) {
                val latLng = LatLng(lat, long)
                googleMap.apply {
                    addMarker(MarkerOptions().position(latLng))
                    animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 9.9f))
                    setOnMapClickListener(this@EditArchiveInformationViewModel)
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

            dateAndLocationProfileItem?.let {
                if (it.locationVO?.locnId != locnVO.locnId) {
                    it.locnId1 = locnVO.locnId
                    it.locationVO = locnVO
                }
            } ?: run {
                dateAndLocationProfileItem = ProfileItem()
                dateAndLocationProfileItem?.archiveId = prefsHelper.getCurrentArchiveId()
                dateAndLocationProfileItem?.fieldName =
                    if (currentArchiveType == ArchiveType.PERSON) ProfileItemName.BIRTH_INFO
                    else ProfileItemName.ESTABLISHED_INFO
                dateAndLocationProfileItem?.locnId1 = locnVO.locnId
                dateAndLocationProfileItem?.locationVO = locnVO
            }
        }
    }

    fun onSaveInfoBtnClick() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        val nameValue = name.value?.trim()
        val aliasesValue = aliases.value?.trim()
        nameAndAliasesProfileItem?.let {
            var updateProfileItem = false
            if (!it.string2.contentEquals(nameValue)) {
                it.string2 = nameValue
                updateProfileItem = true
            }
            if (!it.string3.contentEquals(aliasesValue)) {
                it.string3 = aliasesValue
                updateProfileItem = true
            }
            if (updateProfileItem) addUpdateProfileItem(nameAndAliasesProfileItem!!)

        } ?: run {
            if (nameValue?.isNotEmpty() == true || aliasesValue?.isNotEmpty() == true) {
                nameAndAliasesProfileItem = ProfileItem()
                nameAndAliasesProfileItem?.archiveId = prefsHelper.getCurrentArchiveId()
                nameAndAliasesProfileItem?.fieldName = ProfileItemName.BASIC
                nameAndAliasesProfileItem?.string2 = nameValue
                nameAndAliasesProfileItem?.string3 = aliasesValue
                addUpdateProfileItem(nameAndAliasesProfileItem!!)
            }
        }

        val genderValue = gender.value?.trim()
        genderProfileItem?.let {
            if (!it.string1.contentEquals(genderValue)) {
                it.string1 = genderValue
                addUpdateProfileItem(genderProfileItem!!)
            }
        } ?: run {
            if (genderValue?.isNotEmpty() == true) {
                genderProfileItem = ProfileItem()
                genderProfileItem?.archiveId = prefsHelper.getCurrentArchiveId()
                genderProfileItem?.fieldName = ProfileItemName.GENDER
                genderProfileItem?.string1 = genderValue
                addUpdateProfileItem(genderProfileItem!!)
            }
        }

        val dateValue = date.value?.trim()
        dateAndLocationProfileItem?.let {
            if (!it.day1.contentEquals(dateValue) || isNewLocation) {
                it.day1 = dateValue
                addUpdateProfileItem(dateAndLocationProfileItem!!)
                isNewLocation = false
            }
        } ?: run {
            if (dateValue?.isNotEmpty() == true) {
                dateAndLocationProfileItem = ProfileItem()
                dateAndLocationProfileItem?.archiveId = prefsHelper.getCurrentArchiveId()
                dateAndLocationProfileItem?.fieldName =
                    if (currentArchiveType == ArchiveType.PERSON) ProfileItemName.BIRTH_INFO
                    else ProfileItemName.ESTABLISHED_INFO
                dateAndLocationProfileItem?.day1 = dateValue
                addUpdateProfileItem(dateAndLocationProfileItem!!)
            }
        }
    }

    private fun addUpdateProfileItem(profileItemToUpdate: ProfileItem) {
        isBusy.value = true
        profileRepository.safeAddUpdateProfileItems(
            listOf(profileItemToUpdate), false,
            object : IProfileItemListener {
                override fun onSuccess(profileItem: ProfileItem) {
                    sendEvent(ProfileItemEventAction.UPDATE, profileItem.id.toString())
                    isBusy.value = false
                    profileItemToUpdate.id = profileItem.id
                    showMessage.value = appContext.getString(R.string.edit_about_update_success)
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

    fun getCurrentArchiveType(): ArchiveType = currentArchiveType

    fun getNameLabel(): LiveData<String> = nameLabel
    fun getNameCharsNr(): LiveData<String> = nameCharsNr
    fun getName(): LiveData<String> = name

    fun getAliasesLabel(): LiveData<String> = aliasesLabel
    fun getAliasesCharsNr(): LiveData<String> = aliasesCharsNr
    fun getAliases(): LiveData<String> = aliases

    fun getGender(): LiveData<String> = gender

    fun getDateLabel(): LiveData<String> = dateLabel
    fun getDate(): LiveData<String> = date

    fun getLocationLabel(): LiveData<String> = locationLabel
    fun getLocation(): LiveData<String> = location

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy
    fun getShowMessage(): LiveData<String> = showMessage
    fun getShowError(): LiveData<String> = showError

    fun getShowDatePicker(): LiveData<Void?> = showDatePickerRequest
    fun getShowLocationSearch(): LiveData<ProfileItem?> = showLocationSearchRequest
}