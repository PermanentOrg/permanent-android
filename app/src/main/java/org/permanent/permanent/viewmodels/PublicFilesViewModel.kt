package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.Constants
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.IRecordListener

class PublicFilesViewModel(application: Application) : MyFilesViewModel(application) {

    private val onRootFolderReady = SingleLiveEvent<Void?>()

    init {
        getFolderName().value = Constants.PUBLIC_FILES
    }

    override fun loadRootFilesV1() {
        swipeRefreshLayout.isRefreshing = true
        fileRepository.getPublicRoot(prefsHelper.getCurrentArchiveNr(), object : IRecordListener {
            override fun onSuccess(record: Record) {
                swipeRefreshLayout.isRefreshing = false
                folderPathStack.push(record)
                loadFilesAndUploadsOf(record, forwardNavigation = true)
                loadEnqueuedDownloads(lifecycleOwner)
                onRootFolderReady.call()
            }

            override fun onFailed(error: String?) {
                swipeRefreshLayout.isRefreshing = false
                showMessage.value = error
            }
        })
    }

    override fun resolveRootV2(isStale: () -> Boolean, listener: IRecordListener) {
        fileRepository.getPublicRootV2(isStale, listener)
    }

    override fun resolveRootFailsafe(listener: IRecordListener) {
        fileRepository.getPublicRoot(prefsHelper.getCurrentArchiveNr(), listener)
    }

    // V2 success and its V1 failsafe both land here; the ready event replays pending
    // deep-link navigation and uploads.
    override fun commitRootRecord(record: Record) {
        super.commitRootRecord(record)
        onRootFolderReady.call()
    }

    fun getOnRootFolderReady(): MutableLiveData<Void?> = onRootFolderReady
}
