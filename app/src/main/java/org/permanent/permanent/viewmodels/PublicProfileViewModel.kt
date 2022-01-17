package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Milestone
import org.permanent.permanent.models.ProfileItem
import org.permanent.permanent.models.ProfileItemName
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.repositories.IProfileRepository
import org.permanent.permanent.repositories.ProfileRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class PublicProfileViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val isBusy = MutableLiveData(false)
    private val showMessage = MutableLiveData<String>()
    private val profileItems: MutableList<ProfileItem> = ArrayList()
    private val shortAndLongDescription = MutableLiveData("")
    private val name = MutableLiveData<String>()
    private val nickname = MutableLiveData<String>()
    private val gender = MutableLiveData<String>()
    private val date = MutableLiveData<String>()
    private val location = MutableLiveData<String>()
    private val onlinePresence = MutableLiveData("")
    private val existsMilestones = MutableLiveData(false)
    private val onMilestonesRetrieved = MutableLiveData<MutableList<Milestone>>()
    private val isAboutExtended = MutableLiveData(false)
    private val onReadAbout = SingleLiveEvent<Boolean>()
    private val isOnlinePresenceExtended = MutableLiveData(false)
    private val onShowOnlinePresence = SingleLiveEvent<Boolean>()
    private val onEditAboutRequest = SingleLiveEvent<MutableList<ProfileItem>>()
    private val onEditPersonInformationRequest = SingleLiveEvent<Void>()
    private val onEditMilestonesRequest = SingleLiveEvent<Void>()
    private val onEditOnlinePresenceRequest = SingleLiveEvent<Void>()
    private var profileRepository: IProfileRepository = ProfileRepositoryImpl(application)

    fun getProfileItems() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        isBusy.value = true
        profileRepository.getProfileItemsByArchive(
            prefsHelper.getCurrentArchiveNr(),
            object : IDataListener {
                override fun onSuccess(dataList: List<Datum>?) {
                    isBusy.value = false
                    dataList?.let { displayProfileItems(dataList) }
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    showMessage.value = error
                }
            }
        )
    }

    private fun displayProfileItems(dataList: List<Datum>) {
        val milestones: MutableList<Milestone> = ArrayList()

        for (datum in dataList) {
            val profileItem = ProfileItem(datum.Profile_itemVO)
            profileItems.add(profileItem)
            when (profileItem.fieldName) {
                ProfileItemName.SHORT_DESCRIPTION -> {
                    profileItem.string1?.let { shortAndLongDescription.value = it }
                }
                ProfileItemName.DESCRIPTION -> {
                    profileItem.textData1?.let {
                        shortAndLongDescription.value =
                            when {
                                shortAndLongDescription.value?.isEmpty() == true -> it
                                it.isEmpty() -> shortAndLongDescription.value
                                else -> shortAndLongDescription.value + "\n\n" + it
                            }
                    }
                }
                ProfileItemName.BASIC -> {
                    profileItem.string2?.let { name.value = it }
                    profileItem.string3?.let { nickname.value = it }
                }
                ProfileItemName.GENDER -> {
                    profileItem.string1?.let { gender.value = it }
                }
                ProfileItemName.BIRTH_INFO -> {
                    profileItem.day1?.let { date.value = it }
                    profileItem.locationText?.let { location.value = it }
                }
                ProfileItemName.SOCIAL_MEDIA -> {
                    profileItem.string1?.let {
                        onlinePresence.value =
                            if (onlinePresence.value == "") it else onlinePresence.value + "\n" + it
                    }
                }
                ProfileItemName.MILESTONE -> {
                    milestones.add(Milestone(profileItem))
                }
                else -> {}
            }
        }
        existsMilestones.value = milestones.isNotEmpty()
        onMilestonesRetrieved.value = milestones
    }

    fun onReadAboutBtnClick() {
        onReadAbout.value = !isAboutExtended.value!!
        isAboutExtended.value = !isAboutExtended.value!!
    }

    fun onShowOnlinePresenceBtnClick() {
        onShowOnlinePresence.value = !isOnlinePresenceExtended.value!!
        isOnlinePresenceExtended.value = !isOnlinePresenceExtended.value!!
    }

    fun onEditAboutBtnClick() {
        onEditAboutRequest.value = profileItems
    }

    fun onEditPersonInformationBtnClick() {
        onEditPersonInformationRequest.call()
    }

    fun onEditMilestonesBtnClick() {
        onEditMilestonesRequest.call()
    }

    fun onEditOnlinePresenceBtnClick() {
        onEditOnlinePresenceRequest.call()
    }

    fun getOnEditAboutRequest(): LiveData<MutableList<ProfileItem>> = onEditAboutRequest

    fun getOnEditPersonInformationRequest(): LiveData<Void> = onEditPersonInformationRequest

    fun getOnEditMilestonesRequest(): LiveData<Void> = onEditMilestonesRequest

    fun getOnEditOnlinePresenceRequest(): LiveData<Void> = onEditOnlinePresenceRequest

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getShowMessage(): LiveData<String> = showMessage

    fun getShortAndLongDescription(): LiveData<String> = shortAndLongDescription

    fun getName(): LiveData<String> = name

    fun getNickname(): LiveData<String> = nickname

    fun getGender(): LiveData<String> = gender

    fun getDate(): LiveData<String> = date

    fun getLocation(): LiveData<String> = location

    fun getOnlinePresence(): LiveData<String> = onlinePresence

    fun getExistsMilestones(): MutableLiveData<Boolean> = existsMilestones
    fun getOnMilestonesRetrieved(): LiveData<MutableList<Milestone>> = onMilestonesRetrieved

    fun getIsAboutExtended(): MutableLiveData<Boolean> = isAboutExtended
    fun getOnReadAbout(): LiveData<Boolean> = onReadAbout

    fun getIsOnlinePresenceExtended(): MutableLiveData<Boolean> = isOnlinePresenceExtended
    fun getOnShowOnlinePresence(): LiveData<Boolean> = onShowOnlinePresence
}