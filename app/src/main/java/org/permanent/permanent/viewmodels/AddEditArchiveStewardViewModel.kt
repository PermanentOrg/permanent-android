package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.DirectiveEventAction
import org.permanent.permanent.models.EventAction
import org.permanent.permanent.models.LegacyContactEventAction
import org.permanent.permanent.network.models.ArchiveSteward
import org.permanent.permanent.network.models.IArchiveStewardListener
import org.permanent.permanent.repositories.EventsRepositoryImpl
import org.permanent.permanent.repositories.IEventsRepository
import org.permanent.permanent.repositories.ILegacyPlanningRepository
import org.permanent.permanent.repositories.LegacyPlanningRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class AddEditArchiveStewardViewModel(application: Application) :
    AddEditLegacyEntityViewModel(application) {
    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val onArchiveStewardUpdated = SingleLiveEvent<ArchiveSteward>()
    private var legacyPlanningRepository: ILegacyPlanningRepository =
        LegacyPlanningRepositoryImpl(appContext)
    private var eventsRepository: IEventsRepository = EventsRepositoryImpl(application)
    private var archiveId: Int? = null
    private var archiveSteward: ArchiveSteward? = null

    fun setArchiveId(archiveId: Int?) {
        this.archiveId = archiveId
        name = null
        email = null
        message = null
    }

    fun setArchiveSteward(steward: ArchiveSteward?) {
        archiveSteward = steward
        name = steward?.steward?.name
        email = steward?.steward?.email
        message = steward?.note
    }

    override fun onSaveBtnClick(email: String, name: String?, message: String?) {
        if (archiveSteward?.directiveId.isNullOrEmpty()) {
            archiveId?.let { addArchiveSteward(ArchiveSteward(it, email, message)) }
        } else {
            archiveSteward?.directiveId?.let { editArchiveSteward(it, ArchiveSteward(null, email, message)) }
        }
    }

    private fun addArchiveSteward(archiveSteward: ArchiveSteward) {
        legacyPlanningRepository.addArchiveSteward(
            archiveSteward,
            object : IArchiveStewardListener {

                override fun onSuccess(archiveSteward: ArchiveSteward) {
                    this@AddEditArchiveStewardViewModel.archiveSteward = archiveSteward
                    onArchiveStewardUpdated.value = archiveSteward
                    sendEvent(DirectiveEventAction.CREATE, archiveSteward.archiveId)
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
                    sendEvent(DirectiveEventAction.UPDATE, archiveSteward.archiveId)
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

    fun getOnArchiveStewardUpdated(): MutableLiveData<ArchiveSteward> = onArchiveStewardUpdated
}