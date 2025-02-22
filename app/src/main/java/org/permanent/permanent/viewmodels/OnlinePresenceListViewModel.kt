package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
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

class OnlinePresenceListViewModel(application: Application) :
    ObservableAndroidViewModel(application) {
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val appContext = application.applicationContext
    private val isBusy = MutableLiveData(false)
    private val showMessage = MutableLiveData<String>()
    private val showError = MutableLiveData<String>()
    private var existsOnlinePresences = MutableLiveData(false)
    private var onlinePresences: MutableList<ProfileItem> = ArrayList()
    private var emails: MutableList<ProfileItem> = ArrayList()
    private var socialMedias: MutableList<ProfileItem> = ArrayList()
    private var profileItems: MutableList<ProfileItem> = ArrayList()
    private var onOnlinePresencesRetrieved = MutableLiveData<List<ProfileItem>>()
    private var onAddRequest = SingleLiveEvent<Boolean>()
    private var profileRepository: IProfileRepository = ProfileRepositoryImpl()
    private var eventsRepository: IEventsRepository = EventsRepositoryImpl(application)

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
                    error?.let { showError.value = it }
                }
            }
        )
    }

    fun displayProfileItems(dataList: List<Datum>) {
        onlinePresences = ArrayList()
        emails = ArrayList()
        socialMedias = ArrayList()
        for (datum in dataList) {
            val profileItem = ProfileItem(datum.Profile_itemVO, false)
            profileItems.add(profileItem)
            when (profileItem.fieldName) {
                ProfileItemName.EMAIL -> {
                    profileItem.string1?.let {
                        emails.add(profileItem)
                    }
                }
                ProfileItemName.SOCIAL_MEDIA -> {
                    profileItem.string1?.let {
                        socialMedias.add(profileItem)
                    }
                }
                else -> {}
            }
        }
        onlinePresences.addAll(emails)
        onlinePresences.addAll(socialMedias)
        if (onlinePresences.isEmpty()) {
            existsOnlinePresences.value = false
        } else {
            existsOnlinePresences.value = true
            onOnlinePresencesRetrieved.value = onlinePresences
        }
    }

    fun deleteProfileItem(profileItem: ProfileItem) {
        isBusy.value = true
        profileRepository.deleteProfileItem(profileItem, object : IProfileItemListener {
            override fun onSuccess(profileItem: ProfileItem) {
                sendEvent(ProfileItemEventAction.UPDATE, profileItem.id.toString())
                isBusy.value = false
                getProfileItems()
                showMessage.value = appContext.getString(R.string.online_presence_delete_success)

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

    fun onAddEmailBtnClick() {
        onAddRequest.value = true
    }

    fun onAddSocialMediaBtnClick() {
        onAddRequest.value = false
    }

    fun getExistsOnlinePresences(): MutableLiveData<Boolean> = existsOnlinePresences

    fun getOnOnlinePresencesRetrieved(): LiveData<List<ProfileItem>> = onOnlinePresencesRetrieved

    fun getOnAddRequest(): LiveData<Boolean> = onAddRequest

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getShowMessage(): LiveData<String> = showMessage

    fun getShowError(): LiveData<String> = showError
}
