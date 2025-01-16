package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.R
import org.permanent.permanent.models.AccountEventAction
import org.permanent.permanent.models.Archive
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.repositories.ArchiveRepositoryImpl
import org.permanent.permanent.repositories.EventsRepositoryImpl
import org.permanent.permanent.repositories.IArchiveRepository
import org.permanent.permanent.repositories.IEventsRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class MainViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val archiveThumb = MutableLiveData<String>()
    private val archiveName = MutableLiveData<String>()
    private val errorMessage = MutableLiveData<String>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onViewProfile = SingleLiveEvent<Void?>()
    private val onArchiveSwitched = SingleLiveEvent<Void?>()
    val versionName = MutableLiveData(
        application.getString(
            R.string.version_text, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE.toString()
        )
    )
    private var archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)
    private var eventsRepository: IEventsRepository = EventsRepositoryImpl(application)

    fun switchCurrentArchiveTo(archiveNr: String?) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        archiveNr?.let { archiveNumber ->
            isBusy.value = true
            archiveRepository.switchToArchive(archiveNumber, object : IDataListener {

                override fun onSuccess(dataList: List<Datum>?) {
                    isBusy.value = false
                    if (!dataList.isNullOrEmpty()) {
                        val archive = Archive(dataList[0].ArchiveVO)
                        prefsHelper.saveCurrentArchiveInfo(
                            archive.id,
                            archive.number,
                            archive.type,
                            archive.fullName,
                            archive.thumbURL200,
                            archive.accessRole
                        )
                    }
                    onArchiveSwitched.call()
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    error?.let { errorMessage.value = it }
                }
            })
        }
    }

    fun onViewProfileClick() {
        onViewProfile.call()
    }

    fun updateCurrentArchiveHeader() {
        archiveThumb.value = prefsHelper.getCurrentArchiveThumbURL()
        archiveName.value = prefsHelper.getCurrentArchiveFullName()
    }

    fun sendEvent(action: AccountEventAction, data: Map<String, String> = mapOf()) {
        eventsRepository.sendEventAction(
            eventAction = action,
            accountId = prefsHelper.getAccountId(),
            data = data
        )
    }

    fun getCurrentArchive() : Archive = prefsHelper.getCurrentArchive()

    fun getArchiveThumb(): MutableLiveData<String> = archiveThumb

    fun getArchiveName(): MutableLiveData<String> = archiveName

    fun getErrorMessage(): LiveData<String> = errorMessage

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getOnArchiveSwitched(): LiveData<Void?> = onArchiveSwitched

    fun getOnViewProfile(): LiveData<Void?> = onViewProfile
}