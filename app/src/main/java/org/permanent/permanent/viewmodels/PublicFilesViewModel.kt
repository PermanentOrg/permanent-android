package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.Constants
import org.permanent.permanent.PermanentApplication
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.IRecordListener
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class PublicFilesViewModel(application: Application) : MyFilesViewModel(application) {

    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val onRootFolderReady = SingleLiveEvent<Void?>()

    init {
        getFolderName().value = Constants.PUBLIC_FILES

        PermanentApplication.instance.relocateData?.let {
            setRelocationMode(it)
        }
    }

    override fun loadRootFiles() {
        swipeRefreshLayout.isRefreshing = true
        fileRepository.getPublicRoot(prefsHelper.getCurrentArchiveNr(), object : IRecordListener {
            override fun onSuccess(record: Record) {
                swipeRefreshLayout.isRefreshing = false
                folderPathStack.push(record)
                loadFilesAndUploadsOf(record)
                loadEnqueuedDownloads(lifecycleOwner)
                onRootFolderReady.call()
            }

            override fun onFailed(error: String?) {
                swipeRefreshLayout.isRefreshing = false
                showMessage.value = error
            }
        })
    }

    fun getOnRootFolderReady(): MutableLiveData<Void?> = onRootFolderReady
}