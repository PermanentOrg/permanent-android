package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.models.Archive
import org.permanent.permanent.models.ArchiveType
import org.permanent.permanent.models.EventAction
import org.permanent.permanent.models.ProfileItem
import org.permanent.permanent.models.ProfileItemEventAction
import org.permanent.permanent.models.ProfileItemName
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IProfileItemListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.repositories.EventsRepositoryImpl
import org.permanent.permanent.repositories.IEventsRepository
import org.permanent.permanent.repositories.IProfileRepository
import org.permanent.permanent.repositories.ProfileRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class PublicProfileViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val isBusy = MutableLiveData(false)
    private val showMessage = SingleLiveEvent<String?>()
    private var isViewOnlyMode = MutableLiveData(false)
    private var archiveType: ArchiveType? = null
    private var profileItems: MutableList<ProfileItem> = ArrayList()
    private val isProfilePublic = MutableLiveData<Boolean>()
    private val shortAndLongDescription = MutableLiveData("")
    private val archiveInfoLabel = MutableLiveData("")
    private val nameLabel = MutableLiveData("")
    private val aliasesLabel = MutableLiveData("")
    private val dateLabel = MutableLiveData("")
    private val locationLabel = MutableLiveData("")
    private val name = MutableLiveData("")
    private val aliases = MutableLiveData("")
    private val gender = MutableLiveData("")
    private val date = MutableLiveData("")
    private val location = MutableLiveData("")
    private val onlinePresence = MutableLiveData("")
    private var emails = ""
    private var socialMedias = ""
    private val existsMilestones = MutableLiveData(false)
    private val onMilestonesRetrieved = SingleLiveEvent<MutableList<ProfileItem>>()
    private val isAboutExtended = MutableLiveData(false)
    private val onReadAbout = SingleLiveEvent<Boolean>()
    private val isOnlinePresenceExtended = MutableLiveData(false)
    private val onShowOnlinePresence = SingleLiveEvent<Boolean>()
    private val onEditAboutRequest = SingleLiveEvent<MutableList<ProfileItem>>()
    private val onEditArchiveInformationRequest = SingleLiveEvent<MutableList<ProfileItem>>()
    private val onEditMilestonesRequest = SingleLiveEvent<Void?>()
    private val onEditOnlinePresenceRequest = SingleLiveEvent<Void?>()
    private var profileRepository: IProfileRepository = ProfileRepositoryImpl()
    private var eventsRepository: IEventsRepository = EventsRepositoryImpl(application)
    private var archive: Archive? = null

    fun setArchive(archive: Archive?) {
        this.archive = archive
        archiveType = archive?.type

        archiveInfoLabel.value = appContext.getString(
            R.string.public_profile_archive_information_label,
            archiveType?.toTitleCase()
        )
        nameLabel.value = when (archiveType) {
            ArchiveType.FAMILY -> appContext.getString(R.string.public_profile_family_name_label)
            ArchiveType.ORGANIZATION -> appContext.getString(R.string.public_profile_organization_name_label)
            else -> appContext.getString(R.string.public_profile_full_name_hint)
        }
        aliasesLabel.value = when (archiveType) {
            ArchiveType.FAMILY -> appContext.getString(R.string.public_profile_family_aliases_label)
            ArchiveType.ORGANIZATION -> appContext.getString(R.string.public_profile_organization_aliases_label)
            else -> appContext.getString(R.string.public_profile_person_aliases_label)
        }
        dateLabel.value = when (archiveType) {
            ArchiveType.FAMILY, ArchiveType.ORGANIZATION -> appContext.getString(R.string.public_profile_family_and_organization_date_label)
            else -> appContext.getString(R.string.public_profile_person_date_label)
        }
        locationLabel.value = when (archiveType) {
            ArchiveType.FAMILY, ArchiveType.ORGANIZATION -> appContext.getString(R.string.public_profile_family_and_organization_location_label)
            else -> appContext.getString(R.string.public_profile_person_location_label)
        }
    }

    fun getProfileItems() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        isBusy.value = true
        profileRepository.getProfileItemsByArchive(archive?.number, object : IDataListener {
            override fun onSuccess(dataList: List<Datum>?) {
                isBusy.value = false
                dataList?.let { displayProfileItems(dataList) }
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showMessage.value = error
            }
        })
    }

    private fun displayProfileItems(dataList: List<Datum>) {
        val milestones: MutableList<ProfileItem> = ArrayList()
        shortAndLongDescription.value = ""
        profileItems = ArrayList()
        for (datum in dataList) {
            val profileItem = ProfileItem(datum.Profile_itemVO, true)
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
                        emails = if (emails.isEmpty()) it else emails + "\n" + it
                    }
                }
                ProfileItemName.SOCIAL_MEDIA -> {
                    profileItem.string1?.let {
                        socialMedias = if (socialMedias.isEmpty()) it else socialMedias + "\n" + it
                    }
                }
                ProfileItemName.MILESTONE -> {
                    milestones.add(profileItem)
                }
                else -> {}
            }
        }
        onlinePresence.value =
            if (emails.isEmpty() && socialMedias.isEmpty()) "" else emails + "\n" + socialMedias
        existsMilestones.value = milestones.isNotEmpty()
        onMilestonesRetrieved.value = milestones
        for (profileItem in profileItems) {
            if (profileItem.publicDate == null) {
                isProfilePublic.value = false
                return
            }
        }
        isProfilePublic.value = true
    }

    fun setIsViewOnlyMode() {
        isViewOnlyMode.value = true
    }

    fun onIsProfilePublicChanged(checked: Boolean) {
        if (isViewOnlyMode.value == false) {
            isBusy.value = true
            profileRepository.safeAddUpdateProfileItems(
                getProfileItemsToUpdateVisibility(checked), true,
                object : IProfileItemListener {
                    override fun onSuccess(profileItem: ProfileItem) {
                        sendEvent(ProfileItemEventAction.UPDATE, profileItem.id.toString())
                        isBusy.value = false
                        isProfilePublic.value = checked
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        error?.let { showMessage.value = it }
                    }
                })
        }
    }

    private fun getProfileItemsToUpdateVisibility(checked: Boolean): List<ProfileItem> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = dateFormat.format(Date())

        val filteredItems = profileItems.filter {
            it.fieldName != ProfileItemName.BASIC &&
                    it.fieldName != ProfileItemName.DESCRIPTION &&
                    it.fieldName != ProfileItemName.TIMEZONE
        }
        filteredItems.map {
            it.publicDate = if (checked) date else null
        }

        return filteredItems
    }

    fun sendEvent(action: EventAction, data: Map<String, String> = mapOf()) {
        sendEvent(action, prefsHelper.getAccountId().toString(), data)
    }

    fun sendEvent(action: EventAction, entityId: String?, data: Map<String, String> = mapOf()) {
        eventsRepository.sendEventAction(
            eventAction = action,
            accountId = prefsHelper.getAccountId(),
            entityId = entityId,
            data = data
        )
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
        onEditOnlinePresenceRequest.call()
    }

    fun onEditMilestonesBtnClick() {
        onEditMilestonesRequest.call()
    }

    fun getArchiveType(): ArchiveType? = archiveType

    fun getOnEditAboutRequest(): LiveData<MutableList<ProfileItem>> = onEditAboutRequest

    fun getOnEditArchiveInformationRequest(): LiveData<MutableList<ProfileItem>> =
        onEditArchiveInformationRequest

    fun getOnEditMilestonesRequest(): LiveData<Void?> = onEditMilestonesRequest

    fun getOnEditOnlinePresenceRequest(): LiveData<Void?> = onEditOnlinePresenceRequest

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getShowMessage(): LiveData<String?> = showMessage

    fun getIsViewOnlyMode(): MutableLiveData<Boolean> = isViewOnlyMode

    fun getIsProfilePublic(): LiveData<Boolean> = isProfilePublic

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
    fun getOnMilestonesRetrieved(): LiveData<MutableList<ProfileItem>> = onMilestonesRetrieved

    fun getIsAboutExtended(): MutableLiveData<Boolean> = isAboutExtended
    fun getOnReadAbout(): LiveData<Boolean> = onReadAbout

    fun getIsOnlinePresenceExtended(): MutableLiveData<Boolean> = isOnlinePresenceExtended
    fun getOnShowOnlinePresence(): LiveData<Boolean> = onShowOnlinePresence
}