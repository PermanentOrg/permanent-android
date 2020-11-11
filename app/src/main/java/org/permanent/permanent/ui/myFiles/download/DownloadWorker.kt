package org.permanent.permanent.ui.myFiles.download

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.myFiles.upload.CountingRequestListener
import org.permanent.permanent.ui.myFiles.upload.WORKER_INPUT_FOLDER_LINK_ID_KEY

const val WORKER_INPUT_ARCHIVE_NR_KEY = "worker_input_archive_nr"
const val WORKER_INPUT_ARCHIVE_ID_KEY = "worker_input_archive_id"
const val WORKER_INPUT_RECORD_ID_KEY = "worker_input_record_id"
const val DOWNLOAD_PROGRESS = "download_progress"
class DownloadWorker(val context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    private var fileRepository: IFileRepository = FileRepositoryImpl(context)

    override fun doWork(): Result {
        val folderLinkId = inputData.getInt(WORKER_INPUT_FOLDER_LINK_ID_KEY, 0)
        val archiveNr = inputData.getString(WORKER_INPUT_ARCHIVE_NR_KEY)
        val archiveId = inputData.getInt(WORKER_INPUT_ARCHIVE_ID_KEY, 0)
        val recordId = inputData.getInt(WORKER_INPUT_RECORD_ID_KEY, 0)

        if(archiveNr != null) {
            fileRepository.startDownloading(folderLinkId, archiveNr, archiveId, recordId,
                object : CountingRequestListener {
                    override fun onProgressUpdate(progress: Long) {
                        setProgressAsync(
                            Data.Builder().putInt(DOWNLOAD_PROGRESS, progress.toInt()).build()
                        )
                    }
                })
        } else {
            return Result.failure();
        }
        return Result.success()
    }
}