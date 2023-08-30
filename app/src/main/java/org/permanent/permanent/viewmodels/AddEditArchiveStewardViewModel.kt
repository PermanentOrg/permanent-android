package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.network.models.ArchiveSteward
import org.permanent.permanent.network.models.IArchiveStewardListener
import org.permanent.permanent.repositories.ILegacyPlanningRepository
import org.permanent.permanent.repositories.LegacyPlanningRepositoryImpl

class AddEditArchiveStewardViewModel(application: Application) :
    AddEditLegacyEntityViewModel(application) {
    private val appContext = application.applicationContext
    private val onArchiveStewardUpdated = SingleLiveEvent<ArchiveSteward>()
    private var legacyPlanningRepository: ILegacyPlanningRepository =
        LegacyPlanningRepositoryImpl(appContext)
    private var archiveId: Int? = null
    private var archiveSteward: ArchiveSteward? = null

    fun setArchiveId(archiveId: Int?) {
        this.archiveId = archiveId
        name = null
        email = null
    }

    fun setArchiveSteward(steward: ArchiveSteward?) {
        archiveSteward = steward
        name = steward?.steward?.name
        email = steward?.steward?.email
    }

    override fun onSaveBtnClick(email: String, name: String?, message: String?) {
        if (archiveSteward?.directiveId.isNullOrEmpty()) {
            archiveId?.let { addArchiveSteward(ArchiveSteward(it, email)) }
        } else {
            archiveSteward?.directiveId?.let { editArchiveSteward(it, ArchiveSteward(null, email)) }
        }
    }

    private fun addArchiveSteward(archiveSteward: ArchiveSteward) {
        legacyPlanningRepository.addArchiveSteward(
            archiveSteward,
            object : IArchiveStewardListener {

                override fun onSuccess(archiveSteward: ArchiveSteward) {
                    this@AddEditArchiveStewardViewModel.archiveSteward = archiveSteward
                    onArchiveStewardUpdated.value = archiveSteward
                }

                override fun onFailed(error: String?) {
                    error?.let { showError.value = it }
                }
            })
    }

    private fun editArchiveSteward(directiveId: String, archiveSteward: ArchiveSteward) {
        legacyPlanningRepository.editArchiveSteward(
            directiveId,
            archiveSteward,
            object : IArchiveStewardListener {

                override fun onSuccess(archiveSteward: ArchiveSteward) {
                    onArchiveStewardUpdated.value = archiveSteward
                }

                override fun onFailed(error: String?) {
                    error?.let { showError.value = it }
                }
            })
    }
    fun getOnArchiveStewardUpdated(): MutableLiveData<ArchiveSteward> = onArchiveStewardUpdated
}