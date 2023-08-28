package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.network.ILegacyContactsListener
import org.permanent.permanent.network.models.LegacyContact
import org.permanent.permanent.repositories.ILegacyPlanningRepository
import org.permanent.permanent.repositories.LegacyPlanningRepositoryImpl

class LoadingViewModel(application: Application) :
    ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val onLegacyContactReady = SingleLiveEvent<List<LegacyContact>>()
    private var legacyPlanningRepository: ILegacyPlanningRepository =
        LegacyPlanningRepositoryImpl(appContext)

    init {
        getLegacyContact()
    }

    private fun getLegacyContact() {
        legacyPlanningRepository.getLegacyContact(object : ILegacyContactsListener {
            override fun onSuccess(dataList: List<LegacyContact>) {
                onLegacyContactReady.value = dataList
            }

            override fun onFailed(error: String?) {
            }
        })
    }

    fun getOnLegacyContactReady(): MutableLiveData<List<LegacyContact>> = onLegacyContactReady
}