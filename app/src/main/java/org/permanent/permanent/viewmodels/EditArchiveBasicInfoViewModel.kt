package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.models.EventAction
import org.permanent.permanent.models.ProfileItem
import org.permanent.permanent.models.ProfileItemEventAction
import org.permanent.permanent.models.ProfileItemName
import org.permanent.permanent.network.IProfileItemListener
import org.permanent.permanent.repositories.EventsRepositoryImpl
import org.permanent.permanent.repositories.IEventsRepository
import org.permanent.permanent.repositories.IProfileRepository
import org.permanent.permanent.repositories.ProfileRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class EditArchiveBasicInfoViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val isBusy = MutableLiveData(false)
    private val showMessage = MutableLiveData<String>()
    private val showError = MutableLiveData<String>()
    private val archiveName = MutableLiveData("")
    private val shortDescription = MutableLiveData("")
    private val shortDescriptionCharsNr =
        MutableLiveData(appContext.getString(R.string.edit_about_character_limit, 0))
    private val currentArchiveType = prefsHelper.getCurrentArchiveType()
    private val longDescriptionHint = MutableLiveData(
        application.getString(
            R.string.edit_about_long_description_hint,
            currentArchiveType.toTitleCase()
        )
    )
    private val longDescription = MutableLiveData("")
    private var archiveNameProfileItem: ProfileItem? = null
    private var shortDescriptionProfileItem: ProfileItem? = null
    private var longDescriptionProfileItem: ProfileItem? = null
    private var profileRepository: IProfileRepository = ProfileRepositoryImpl()
    private var eventsRepository: IEventsRepository = EventsRepositoryImpl(application)

    fun displayProfileItems(profileItems: MutableList<ProfileItem>) {
        for (profileItem in profileItems) {
            when (profileItem.fieldName) {
                ProfileItemName.BASIC -> {
                    archiveNameProfileItem = profileItem
                    profileItem.string1?.let { archiveName.value = it }
                }
                ProfileItemName.SHORT_DESCRIPTION -> {
                    shortDescriptionProfileItem = profileItem
                    profileItem.string1?.let { shortDescription.value = it }
                }
                ProfileItemName.DESCRIPTION -> {
                    longDescriptionProfileItem = profileItem
                    profileItem.textData1?.let { longDescription.value = it }
                }
                else -> {}
            }
        }
    }

    fun onSaveInfoBtnClick() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        archiveNameProfileItem?.let {
            val archiveName = archiveName.value?.trim()
            if (!it.string1.contentEquals(archiveName) && archiveName?.isNotEmpty() == true) {
                it.string1 = archiveName
                addUpdateProfileItem(archiveNameProfileItem!!)
            }
        }

        shortDescriptionProfileItem?.let {
            if (!it.string1.contentEquals(shortDescription.value?.trim())) {
                it.string1 = shortDescription.value?.trim()
                addUpdateProfileItem(shortDescriptionProfileItem!!)
            }
        } ?: run {
            if (shortDescription.value?.trim()?.isNotEmpty() == true) {
                shortDescriptionProfileItem = ProfileItem()
                shortDescriptionProfileItem?.archiveId = prefsHelper.getCurrentArchiveId()
                shortDescriptionProfileItem?.fieldName = ProfileItemName.SHORT_DESCRIPTION
                shortDescriptionProfileItem?.string1 = shortDescription.value?.trim()
                addUpdateProfileItem(shortDescriptionProfileItem!!)
            }
        }

        longDescriptionProfileItem?.let {
            if (!it.textData1.contentEquals(longDescription.value?.trim())) {
                it.textData1 = longDescription.value?.trim()
                addUpdateProfileItem(longDescriptionProfileItem!!)
            }
        } ?: run {
            if (longDescription.value?.trim()?.isNotEmpty() == true) {
                longDescriptionProfileItem = ProfileItem()
                longDescriptionProfileItem?.archiveId = prefsHelper.getCurrentArchiveId()
                longDescriptionProfileItem?.fieldName = ProfileItemName.DESCRIPTION
                longDescriptionProfileItem?.textData1 = longDescription.value?.trim()
                addUpdateProfileItem(longDescriptionProfileItem!!)
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
                    profileItemToUpdate.id = profileItem.id
                    showMessage.value = appContext.getString(R.string.edit_about_update_success)
                    if (profileItemToUpdate.fieldName == ProfileItemName.BASIC) {
                        prefsHelper.updateCurrentArchiveName(profileItemToUpdate.string1)
                    }
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    error?.let { showError.value = it }
                }
            })
    }

    fun onArchiveNameTextChanged(text: Editable) {
        archiveName.value = text.toString()
    }

    fun onShortDescriptionTextChanged(text: Editable) {
        shortDescription.value = text.toString()
        shortDescriptionCharsNr.value =
            appContext.getString(R.string.edit_about_character_limit, text.length)
    }

    fun onLongDescriptionTextChanged(text: Editable) {
        longDescription.value = text.toString()
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

    fun getArchiveName(): LiveData<String> = archiveName

    fun getShortDescription(): LiveData<String> = shortDescription

    fun getShortDescriptionCharsNr(): LiveData<String> = shortDescriptionCharsNr

    fun getLongDescriptionHint(): LiveData<String> = longDescriptionHint

    fun getLongDescription(): LiveData<String> = longDescription
}