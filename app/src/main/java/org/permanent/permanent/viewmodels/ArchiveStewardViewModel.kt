package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.network.IArchiveStewardsListener
import org.permanent.permanent.network.models.ArchiveSteward
import org.permanent.permanent.repositories.ILegacyPlanningRepository
import org.permanent.permanent.repositories.LegacyPlanningRepositoryImpl

class ArchiveStewardViewModel(application: Application) : ObservableAndroidViewModel(application)  {

    private val appContext = application.applicationContext
    private val onArchiveStewardReady = SingleLiveEvent<ArchiveSteward?>()
    private var legacyPlanningRepository: ILegacyPlanningRepository =
        LegacyPlanningRepositoryImpl(appContext)
    val contactName = MutableLiveData<String?>()
    val contactEmail = MutableLiveData<String?>()

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

    fun onArchiveStewardUpdated(archiveSteward: ArchiveSteward) {
        contactName.value = archiveSteward.steward?.name
        contactEmail.value = archiveSteward.steward?.email
    }

    fun getOnArchiveStewardReady(): MutableLiveData<ArchiveSteward?> = onArchiveStewardReady
}