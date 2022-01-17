package org.permanent.permanent.viewmodels

import android.app.Application
import org.permanent.permanent.Constants
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.IRecordListener

class PublicFilesViewModel(application: Application) : MyFilesViewModel(application) {

    init {
        getFolderName().value = Constants.PUBLIC_FILES_FOLDER
    }

    override fun loadRootFiles() {
        swipeRefreshLayout.isRefreshing = true
        fileRepository.getPublicRoot(object : IRecordListener {
            override fun onSuccess(record: Record) {
                swipeRefreshLayout.isRefreshing = false
                folderPathStack.push(record)
                loadFilesAndUploadsOf(record)
                loadEnqueuedDownloads(lifecycleOwner)
            }

            override fun onFailed(error: String?) {
                swipeRefreshLayout.isRefreshing = false
                showMessage.value = error
            }
        })
    }
}