package org.permanent.permanent.viewmodels

import android.app.Application
import android.app.DatePickerDialog
import android.content.Context
import android.text.Editable
import android.widget.DatePicker
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.models.ProfileItem
import org.permanent.permanent.models.ProfileItemName
import org.permanent.permanent.network.IProfileItemListener
import org.permanent.permanent.network.models.LocnVO
import org.permanent.permanent.repositories.IProfileRepository
import org.permanent.permanent.repositories.ProfileRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import java.text.DecimalFormat
import java.text.NumberFormat

class EditArchiveInformationViewModel(application: Application) :
    ObservableAndroidViewModel(application), DatePickerDialog.OnDateSetListener {
    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val isBusy = MutableLiveData(false)
    private val showMessage = MutableLiveData<String?>()
    private val showError = MutableLiveData<String?>()

    //    private val currentArchiveType = prefsHelper.getCurrentArchiveType()
    private val name = MutableLiveData("")
    private val aliases = MutableLiveData("")
    private val gender = MutableLiveData("")
    private val date = MutableLiveData("")
    private val location = MutableLiveData("")
    private var isNewLocation = false
    private val showDatePickerRequest = SingleLiveEvent<Void>()
    private val showLocationSearchRequest = SingleLiveEvent<ProfileItem?>()
    private var nameAndAliasesProfileItem: ProfileItem? = null
    private var genderProfileItem: ProfileItem? = null
    private var dateAndLocationProfileItem: ProfileItem? = null
    private var profileRepository: IProfileRepository = ProfileRepositoryImpl(application)

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
                ProfileItemName.BIRTH_INFO -> {
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
    }

    fun onAliasesTextChanged(text: Editable) {
        aliases.value = text.toString()
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

    fun onLocationClick() {
        showLocationSearchRequest.value = dateAndLocationProfileItem
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
                dateAndLocationProfileItem?.fieldName = ProfileItemName.BIRTH_INFO
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
                dateAndLocationProfileItem?.fieldName = ProfileItemName.BIRTH_INFO
                dateAndLocationProfileItem?.day1 = dateValue
                addUpdateProfileItem(dateAndLocationProfileItem!!)
            }
        }
    }

    private fun addUpdateProfileItem(profileItemToUpdate: ProfileItem) {
        isBusy.value = true
        profileRepository.safeAddUpdateProfileItems(
            profileItemToUpdate,
            object : IProfileItemListener {
                override fun onSuccess(profileItem: ProfileItem) {
                    isBusy.value = false
                    profileItemToUpdate.id = profileItem.id
                    showMessage.value = appContext.getString(R.string.edit_about_update_success)
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    showError.value = error
                }
            })
    }

    fun getName(): LiveData<String> = name

    fun getAliases(): LiveData<String> = aliases

    fun getGender(): LiveData<String> = gender

    fun getDate(): LiveData<String> = date
    fun getLocation(): LiveData<String> = location
    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getShowMessage(): LiveData<String?> = showMessage

    fun getShowError(): LiveData<String?> = showError
    fun getShowDatePicker(): LiveData<Void> = showDatePickerRequest
    fun getShowLocationSearch(): LiveData<ProfileItem?> = showLocationSearchRequest
}