package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Archive
import org.permanent.permanent.network.ILegacyArchiveListener
import org.permanent.permanent.network.models.ArchiveSteward
import org.permanent.permanent.repositories.ILegacyPlanningRepository
import org.permanent.permanent.repositories.LegacyPlanningRepositoryImpl

class ArchiveStewardViewModel(application: Application) : ObservableAndroidViewModel(application)  {

    private val appContext = application.applicationContext
    private val onArchiveStewardReady = SingleLiveEvent<ArchiveSteward?>()
    private var legacyPlanningRepository: ILegacyPlanningRepository =
        LegacyPlanningRepositoryImpl(appContext)
    var archive: Archive? = null

    fun getArchiveSteward(archiveId: Int) {
        legacyPlanningRepository.getArchiveSteward(archiveId = archiveId, object :
            ILegacyArchiveListener {
            override fun onSuccess(dataList: List<ArchiveSteward>) {
                val steward: ArchiveSteward? = dataList.firstOrNull()
                onArchiveStewardReady.value = steward
            }

            override fun onFailed(error: String?) {

            }
        })
    }

    fun getOnArchiveStewardReady(): MutableLiveData<ArchiveSteward?> = onArchiveStewardReady
}