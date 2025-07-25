package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.EventAction
import org.permanent.permanent.network.IArchiveStewardsListener
import org.permanent.permanent.network.ILegacyContactsListener
import org.permanent.permanent.network.models.ArchiveSteward
import org.permanent.permanent.network.models.LegacyContact
import org.permanent.permanent.repositories.EventsRepositoryImpl
import org.permanent.permanent.repositories.IEventsRepository
import org.permanent.permanent.repositories.ILegacyPlanningRepository
import org.permanent.permanent.repositories.LegacyPlanningRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class ArchiveStewardViewModel(application: Application) : ObservableAndroidViewModel(application)  {

    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val _hasLegacyContact = MutableLiveData<Boolean>()
    val hasLegacyContact: LiveData<Boolean> = _hasLegacyContact
    private val onArchiveStewardReady = SingleLiveEvent<ArchiveSteward?>()
    private var legacyPlanningRepository: ILegacyPlanningRepository =
        LegacyPlanningRepositoryImpl(appContext)
    private var eventsRepository: IEventsRepository = EventsRepositoryImpl(application)
    val contactName = MutableLiveData<String?>()
    val contactEmail = MutableLiveData<String?>()

    fun getLegacyContact() {
        legacyPlanningRepository.getLegacyContact(object : ILegacyContactsListener {
            override fun onSuccess(dataList: List<LegacyContact>) {
                _hasLegacyContact.postValue(dataList.isNotEmpty())
            }

            override fun onFailed(error: String?) {
            }
        })
    }

    fun getArchiveSteward(archiveId: Int) {
        legacyPlanningRepository.getArchiveSteward(archiveId = archiveId, object :
            IArchiveStewardsListener {
            override fun onSuccess(archiveStewards: List<ArchiveSteward>) {
                onArchiveStewardReady.value = archiveStewards.firstOrNull()
                contactName.value = archiveStewards.firstOrNull()?.steward?.name
                contactEmail.value = archiveStewards.firstOrNull()?.steward?.email
            }

            override fun onFailed(error: String?) {
            }
        })
    }

    fun sendEvent(action: EventAction, data: Map<String, String> = mapOf()) {
        eventsRepository.sendEventAction(
            eventAction = action,
            accountId = prefsHelper.getAccountId(),
            data = data
        )
    }

    fun onArchiveStewardUpdated(archiveSteward: ArchiveSteward) {
        contactName.value = archiveSteward.steward?.name
        contactEmail.value = archiveSteward.steward?.email
    }

    fun getOnArchiveStewardReady(): MutableLiveData<ArchiveSteward?> = onArchiveStewardReady
}