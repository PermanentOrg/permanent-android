package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.network.ILegacyContactsListener
import org.permanent.permanent.network.models.LegacyContact
import org.permanent.permanent.repositories.ILegacyPlanningRepository
import org.permanent.permanent.repositories.LegacyPlanningRepositoryImpl

class LegacyContactViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val onLegacyContactReady = SingleLiveEvent<LegacyContact?>()
    private var legacyPlanningRepository: ILegacyPlanningRepository =
        LegacyPlanningRepositoryImpl(appContext)
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

    fun onLegacyContactUpdated(contact: LegacyContact) {
        contactName.value = contact.name
        contactEmail.value = contact.email
    }
    fun getOnLegacyContactReady(): MutableLiveData<LegacyContact?> = onLegacyContactReady
}