package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.EventAction
import org.permanent.permanent.network.ILegacyContactsListener
import org.permanent.permanent.network.models.LegacyContact
import org.permanent.permanent.repositories.EventsRepositoryImpl
import org.permanent.permanent.repositories.IEventsRepository
import org.permanent.permanent.repositories.ILegacyPlanningRepository
import org.permanent.permanent.repositories.LegacyPlanningRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class LegacyContactViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val onLegacyContactReady = SingleLiveEvent<LegacyContact?>()
    private var legacyPlanningRepository: ILegacyPlanningRepository =
        LegacyPlanningRepositoryImpl(appContext)
    private var eventsRepository: IEventsRepository = EventsRepositoryImpl(application)
    val contactName = MutableLiveData<String?>()
    val contactEmail = MutableLiveData<String?>()

    init {
        getLegacyContact()
    }

    private fun getLegacyContact() {
        legacyPlanningRepository.getLegacyContact(object : ILegacyContactsListener {
            override fun onSuccess(legacyContacts: List<LegacyContact>) {
                onLegacyContactReady.value = legacyContacts.firstOrNull()
                contactName.value = legacyContacts.firstOrNull()?.name
                contactEmail.value = legacyContacts.firstOrNull()?.email
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

    fun onLegacyContactUpdated(contact: LegacyContact) {
        contactName.value = contact.name
        contactEmail.value = contact.email
    }

    fun getOnLegacyContactReady(): MutableLiveData<LegacyContact?> = onLegacyContactReady
}