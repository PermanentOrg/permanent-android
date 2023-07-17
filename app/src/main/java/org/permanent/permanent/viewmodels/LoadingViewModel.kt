package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.ILegacyPlanningRepository
import org.permanent.permanent.repositories.LegacyPlanningRepositoryImpl

class LoadingViewModel(application: Application) :
    ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val onLegacyContactReady = SingleLiveEvent<Void?>()
    private var legacyPlanningRepository: ILegacyPlanningRepository =
        LegacyPlanningRepositoryImpl(appContext)

    init {
        getLegacyContact()
    }

    private fun getLegacyContact() {
        legacyPlanningRepository.getLegacyContact(object : IResponseListener {
            override fun onSuccess(message: String?) {
                onLegacyContactReady.call()
            }

            override fun onFailed(error: String?) {
            }
        })
    }

    fun getOnLegacyContactReady(): MutableLiveData<Void?> = onLegacyContactReady
}