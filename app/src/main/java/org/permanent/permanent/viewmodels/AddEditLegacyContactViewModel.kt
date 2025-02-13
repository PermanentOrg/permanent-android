package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.models.EventAction
import org.permanent.permanent.models.LegacyContactEventAction
import org.permanent.permanent.network.ILegacyContactListener
import org.permanent.permanent.network.models.LegacyContact
import org.permanent.permanent.repositories.EventsRepositoryImpl
import org.permanent.permanent.repositories.IEventsRepository
import org.permanent.permanent.repositories.ILegacyPlanningRepository
import org.permanent.permanent.repositories.LegacyPlanningRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class AddEditLegacyContactViewModel(application: Application) :
    AddEditLegacyEntityViewModel(application) {
    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val onLegacyContactUpdated = SingleLiveEvent<LegacyContact>()
    private var legacyPlanningRepository: ILegacyPlanningRepository =
        LegacyPlanningRepositoryImpl(appContext)
    private var eventsRepository: IEventsRepository = EventsRepositoryImpl(application)
    private var legacyContact: LegacyContact? = null

    fun setContact(legacyContact: LegacyContact?) {
        this.legacyContact = legacyContact
        name = legacyContact?.name
        email = legacyContact?.email
    }

    override fun onSaveBtnClick(email: String, name: String?, message: String?) {
        val contact = LegacyContact(email, name)

        if (legacyContact == null) {
            addLegacyContact(contact)
        } else {
            val legacyContactId = legacyContact?.legacyContactId

            if (legacyContactId != null) editLegacyContact(legacyContactId, contact)
            else showError.value = appContext.getString(R.string.generic_error)
        }
    }

    private fun addLegacyContact(contact: LegacyContact) {
        legacyPlanningRepository.addLegacyContact(contact, object : ILegacyContactListener {
            override fun onSuccess(contact: LegacyContact) {
                legacyContact = contact
                onLegacyContactUpdated.value = contact
                sendEvent(LegacyContactEventAction.CREATE, contact.legacyContactId)
            }

            override fun onFailed(error: String?) {
                error?.let { showError.value = it }
            }
        })
    }

    private fun editLegacyContact(legacyContactId: String, legacyContact: LegacyContact) {
        legacyPlanningRepository.editLegacyContact(
            legacyContactId,
            legacyContact,
            object : ILegacyContactListener {
                override fun onSuccess(contact: LegacyContact) {
                    onLegacyContactUpdated.value = contact
                    sendEvent(LegacyContactEventAction.UPDATE, contact.legacyContactId)
                }

                override fun onFailed(error: String?) {
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

    fun getOnLegacyContactUpdated(): MutableLiveData<LegacyContact> = onLegacyContactUpdated
}