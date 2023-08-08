package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.network.ILegacyAccountListener
import org.permanent.permanent.network.models.LegacySteward
import org.permanent.permanent.repositories.ILegacyPlanningRepository
import org.permanent.permanent.repositories.LegacyPlanningRepositoryImpl

class LoadingViewModel(application: Application) :
    ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val onLegacyContactReady = SingleLiveEvent<List<LegacySteward>>()
    private var legacyPlanningRepository: ILegacyPlanningRepository =
        LegacyPlanningRepositoryImpl(appContext)

    init {
        getLegacyContact()
    }

    private fun getLegacyContact() {
        legacyPlanningRepository.getLegacyContact(object : ILegacyAccountListener {
            override fun onSuccess(dataList: List<LegacySteward>) {
                onLegacyContactReady.value = dataList
            }

            override fun onFailed(error: String?) {
            }
        })
    }

    fun getOnLegacyContactReady(): MutableLiveData<List<LegacySteward>> = onLegacyContactReady
}