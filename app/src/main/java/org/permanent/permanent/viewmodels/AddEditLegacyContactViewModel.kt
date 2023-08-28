package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.network.ILegacyContactListener
import org.permanent.permanent.network.models.LegacyContact
import org.permanent.permanent.repositories.ILegacyPlanningRepository
import org.permanent.permanent.repositories.LegacyPlanningRepositoryImpl

class AddEditLegacyContactViewModel(application: Application) :
    ObservableAndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val onLegacyContactUpdated = SingleLiveEvent<LegacyContact>()
    private var legacyPlanningRepository: ILegacyPlanningRepository =
        LegacyPlanningRepositoryImpl(appContext)
    var legacyContact: LegacyContact? = null
    val showError = MutableLiveData<String>()

    fun onSaveLegacyContact(email: String, name: String) {
        val contact = LegacyContact(email, name)

        if (legacyContact == null) {
            addLegacyContact(contact)
        } else {
            editLegacyContact(legacyContact?.legacyContactId!!, contact)
        }
    }

    private fun addLegacyContact(contact: LegacyContact) {
        legacyPlanningRepository.addLegacyContact(contact, object : ILegacyContactListener {
            override fun onSuccess(contact: LegacyContact) {
                onLegacyContactUpdated.value = contact
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
                }

                override fun onFailed(error: String?) {
                    error?.let { showError.value = it }
                }
            })
    }

    fun getOnLegacyContactUpdated(): MutableLiveData<LegacyContact> = onLegacyContactUpdated
}