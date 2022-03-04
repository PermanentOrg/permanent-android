package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.models.ArchiveType
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
    private val showMessage = MutableLiveData<String?>()
    private val currentArchiveType = prefsHelper.getCurrentArchiveType()
    private var profileItems: MutableList<ProfileItem> = ArrayList()
    private val shortAndLongDescription = MutableLiveData("")
    private val archiveInfoLabel = MutableLiveData(
        application.getString(
            R.string.public_profile_archive_information_label,
            currentArchiveType.toTitleCase()
        )
    )
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
    private val name = MutableLiveData<String>()
    private val aliases = MutableLiveData<String>()
    private val gender = MutableLiveData<String>()
    private val date = MutableLiveData<String>()
    private val location = MutableLiveData<String>()
    private val onlinePresence = MutableLiveData("")
    private val emails = MutableLiveData("")
    private val socialMedias = MutableLiveData("")
    private val existsMilestones = MutableLiveData(false)
    private val onMilestonesRetrieved = MutableLiveData<MutableList<Milestone>>()
    private val isAboutExtended = MutableLiveData(false)
    private val onReadAbout = SingleLiveEvent<Boolean>()
    private val isOnlinePresenceExtended = MutableLiveData(false)
    private val onShowOnlinePresence = SingleLiveEvent<Boolean>()
    private val onEditAboutRequest = SingleLiveEvent<MutableList<ProfileItem>>()
    private val onEditArchiveInformationRequest = SingleLiveEvent<MutableList<ProfileItem>>()
    private val onEditMilestonesRequest = SingleLiveEvent<Void>()
    private val onEditOnlinePresenceRequest = SingleLiveEvent<MutableList<ProfileItem>>()
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
        shortAndLongDescription.value = ""
        profileItems = ArrayList()
        for (datum in dataList) {
            val profileItem = ProfileItem(datum.Profile_itemVO)
            profileItems.add(profileItem)
            when (profileItem.fieldName) {
                ProfileItemName.SHORT_DESCRIPTION -> {
                    profileItem.string1?.let {
                        shortAndLongDescription.value =
                            when {
                                shortAndLongDescription.value?.isEmpty() == true -> it
                                it.isEmpty() -> shortAndLongDescription.value
                                else -> it + "\n\n" + shortAndLongDescription.value
                            }
                    }
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
                    profileItem.string3?.let { aliases.value = it }
                }
                ProfileItemName.GENDER -> {
                    profileItem.string1?.let { gender.value = it }
                }
                ProfileItemName.BIRTH_INFO, ProfileItemName.ESTABLISHED_INFO -> {
                    profileItem.day1?.let { date.value = it }
                    profileItem.locationVO?.getUIAddress()?.let { location.value = it }
                }
                ProfileItemName.EMAIL -> {
                    profileItem.string1?.let {
                        emails.value =
                            if (emails.value == "") it else emails.value + "\n" + it
                    }
                }
                ProfileItemName.SOCIAL_MEDIA -> {
                    profileItem.string1?.let {
                        socialMedias.value =
                            if (socialMedias.value == "") it else socialMedias.value + "\n" + it
                    }
                }
                ProfileItemName.MILESTONE -> {
                    milestones.add(Milestone(profileItem, true))
                }
                else -> {}
            }
        }
        onlinePresence.value = emails.value + "\n" + socialMedias.value
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

    fun onEditArchiveInformationBtnClick() {
        onEditArchiveInformationRequest.value = profileItems
    }

    fun onEditOnlinePresenceBtnClick() {
        onEditOnlinePresenceRequest.value = profileItems
    }

    fun onEditMilestonesBtnClick() {
        onEditMilestonesRequest.call()
    }

    fun getCurrentArchiveType(): ArchiveType = currentArchiveType

    fun getOnEditAboutRequest(): LiveData<MutableList<ProfileItem>> = onEditAboutRequest

    fun getOnEditArchiveInformationRequest(): LiveData<MutableList<ProfileItem>> =
        onEditArchiveInformationRequest

    fun getOnEditMilestonesRequest(): LiveData<Void> = onEditMilestonesRequest

    fun getOnEditOnlinePresenceRequest(): LiveData<MutableList<ProfileItem>> =
        onEditOnlinePresenceRequest

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getShowMessage(): LiveData<String?> = showMessage

    fun getShortAndLongDescription(): LiveData<String> = shortAndLongDescription

    fun getArchiveInfoLabel(): LiveData<String> = archiveInfoLabel

    fun getNameLabel(): LiveData<String> = nameLabel
    fun getName(): LiveData<String> = name

    fun getAliasesLabel(): LiveData<String> = aliasesLabel
    fun getAliases(): LiveData<String> = aliases

    fun getGender(): LiveData<String> = gender

    fun getDateLabel(): LiveData<String> = dateLabel
    fun getDate(): LiveData<String> = date

    fun getLocationLabel(): LiveData<String> = locationLabel
    fun getLocation(): LiveData<String> = location

    fun getOnlinePresence(): LiveData<String> = onlinePresence

    fun getExistsMilestones(): MutableLiveData<Boolean> = existsMilestones
    fun getOnMilestonesRetrieved(): LiveData<MutableList<Milestone>> = onMilestonesRetrieved

    fun getIsAboutExtended(): MutableLiveData<Boolean> = isAboutExtended
    fun getOnReadAbout(): LiveData<Boolean> = onReadAbout

    fun getIsOnlinePresenceExtended(): MutableLiveData<Boolean> = isOnlinePresenceExtended
    fun getOnShowOnlinePresence(): LiveData<Boolean> = onShowOnlinePresence
}