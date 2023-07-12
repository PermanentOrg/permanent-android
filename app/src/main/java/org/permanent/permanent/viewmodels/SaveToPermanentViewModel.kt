package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.models.File
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.Workspace

class SaveToPermanentViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val currentArchiveThumb =
        MutableLiveData<String>(prefsHelper.getCurrentArchiveThumbURL())
    private val currentArchiveName =
        MutableLiveData<String>(prefsHelper.getCurrentArchiveFullName())
    private val destinationFolderName =
        MutableLiveData(appContext.getString(R.string.menu_drawer_private_files))
    private val onUploadRequest = SingleLiveEvent<Void?>()
    private val onChangeDestinationFolderRequest = SingleLiveEvent<Void?>()
    private val onChangeDestinationArchiveRequest = SingleLiveEvent<Void?>()
    private val onCancelRequest = SingleLiveEvent<Void?>()

    fun updateCurrentArchive() {
        currentArchiveThumb.value = prefsHelper.getCurrentArchiveThumbURL()
        currentArchiveName.value = prefsHelper.getCurrentArchiveFullName()
    }

    fun onCancelBtnClick() {
        onCancelRequest.call()
    }

    fun onUploadBtnClick() {
        onUploadRequest.call()
    }

    fun onDestinationArchiveClick() {
        onChangeDestinationArchiveRequest.call()
    }

    fun onDestinationFolderClick() {
        onChangeDestinationFolderRequest.call()
    }

    fun changeDestinationFolderTo(workspace: Workspace, record: Record?) {
        if (record != null) destinationFolderName.value = record.displayName
        else destinationFolderName.value = when (workspace) {
            Workspace.PUBLIC_FILES -> appContext.getString(R.string.menu_drawer_public_files)
            else -> appContext.getString(R.string.menu_drawer_private_files)
        }
    }

    fun getFiles(uris: ArrayList<Uri>): ArrayList<File> {
        val files = ArrayList<File>()
        for (uri in uris) files.add(File(appContext, uri))
        return files
    }

    fun getCurrentArchiveThumb(): MutableLiveData<String> = currentArchiveThumb

    fun getCurrentArchiveName(): MutableLiveData<String> = currentArchiveName

    fun getDestinationFolderName(): MutableLiveData<String> = destinationFolderName

    fun getOnUploadRequest(): MutableLiveData<Void?> = onUploadRequest

    fun getOnChangeDestinationFolderRequest(): MutableLiveData<Void?> =
        onChangeDestinationFolderRequest

    fun getOnChangeDestinationArchiveRequest(): MutableLiveData<Void?> =
        onChangeDestinationArchiveRequest

    fun getOnCancelRequest(): MutableLiveData<Void?> = onCancelRequest
}
