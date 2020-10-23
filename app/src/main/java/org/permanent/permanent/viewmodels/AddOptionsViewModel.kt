package org.permanent.permanent.viewmodels

import android.app.Application
import android.net.Uri
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkContinuation
import androidx.work.WorkManager
import org.permanent.permanent.ui.myFiles.UploadWorker
import org.permanent.permanent.ui.myFiles.WORKER_INPUT_URI_KEY
import org.permanent.permanent.ui.myFiles.WORKER_TAG_UPLOAD

class AddOptionsViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val workManager: WorkManager = WorkManager.getInstance()

    fun onNewFolderBtnClick() {
    }

    fun setupUploadWorker(originalUri: Uri) {
        val builder: Data.Builder = Data.Builder()
        builder.putString(WORKER_INPUT_URI_KEY, originalUri.toString())
        val request: OneTimeWorkRequest = OneTimeWorkRequest.Builder(UploadWorker::class.java)
            .addTag(WORKER_TAG_UPLOAD)
            .setInputData(builder.build()).build()
        workManager.enqueue(request)
    }

    /**
     * Sets up an upload worker to run for each uri
     * This runs in sequence rather than in parallel because it's the easiest way to allow the user
     * to cancel uploads
     * WorkManager cancel does not guarantee already running tasks will be stopped. If these workers
     * are set up in parallel they will all be in a running state and thus uncancellable even though
     * for the most part they upload one by one. Manually stopping them is an alternative approach.
     *
     * @param uris - list of uris to upload
     */
    fun setupUploadWorkers(uris: List<Uri>) {
        if (uris.isNotEmpty()) {
            var continuation: WorkContinuation =
                workManager.beginWith(getUploadRequest(uris[0].toString()))
            for (i in 1 until uris.size) {
                continuation = continuation.then(getUploadRequest(uris[i].toString()))
            }
            continuation.enqueue()
        }
    }

    private fun getUploadRequest(uri: String): OneTimeWorkRequest {
        val builder = Data.Builder().apply { putString(WORKER_INPUT_URI_KEY, uri) }

        return OneTimeWorkRequest.Builder(UploadWorker::class.java)
            .addTag(WORKER_TAG_UPLOAD)
            .setInputData(builder.build())
            .build()
    }
}