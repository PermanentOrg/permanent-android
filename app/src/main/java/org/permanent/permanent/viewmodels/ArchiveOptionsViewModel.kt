package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Archive
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class ArchiveOptionsViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val prefsHelper = PreferencesHelper(
        application.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val isCurrentArchive = MutableLiveData(false)
    private val accessRole = MutableLiveData<AccessRole>()
    private val archiveName = MutableLiveData<String>()
    private val archiveThumb = MutableLiveData<String>()

    fun setArchive(archive: Archive?) {
        isCurrentArchive.value = prefsHelper.getCurrentArchiveId() == archive?.id
        accessRole.value = archive?.accessRole
        archiveName.value = archive?.fullName
        archiveThumb.value = archive?.thumbURL200
    }

    fun getIsCurrentArchive(): MutableLiveData<Boolean> = isCurrentArchive

    fun getAccessRole(): MutableLiveData<AccessRole> = accessRole

    fun getArchiveName(): MutableLiveData<String> = archiveName

    fun getArchiveThumb(): MutableLiveData<String> = archiveThumb
}