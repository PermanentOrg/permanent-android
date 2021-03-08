package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Intent
import android.os.Environment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.rajat.pdfviewer.PdfViewerActivity
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.models.Download
import org.permanent.permanent.models.FileType
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.models.Upload
import org.permanent.permanent.network.models.FileData
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.myFiles.OnFinishedListener
import org.permanent.permanent.ui.myFiles.SortType
import org.permanent.permanent.ui.myFiles.download.DownloadQueue
import org.permanent.permanent.ui.myFiles.download.DownloadableRecord
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*

class SharedXMeViewModel(application: Application
) : ObservableAndroidViewModel(application), OnFinishedListener {

    private val appContext = application.applicationContext
    private lateinit var lifecycleOwner: LifecycleOwner
    private lateinit var downloadQueue: DownloadQueue
    val isRoot = MutableLiveData(true)
    private val folderName = MutableLiveData(Constants.MY_FILES_FOLDER)
    private val isBusy = MutableLiveData(false)
    private val showMessage = SingleLiveEvent<String>()
    var existsShares = MutableLiveData(false)
    private var folderPathStack: Stack<DownloadableRecord> = Stack()
    private val onRecordsRetrieved = SingleLiveEvent<MutableList<DownloadableRecord>>()
    private val onRootSharesNeeded = SingleLiveEvent<Void>()
    private val onFileViewRequest = SingleLiveEvent<FileData>()
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)

    fun setLifecycleOwner(lifecycleOwner: LifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner
        downloadQueue = DownloadQueue(getApplication(), lifecycleOwner, this)
    }

    fun getIsRoot(): MutableLiveData<Boolean> {
        return isRoot
    }

    fun getFolderName(): MutableLiveData<String> {
        return folderName
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getShowMessage(): LiveData<String> {
        return showMessage
    }

    fun getOnRecordsRetrieved(): LiveData<MutableList<DownloadableRecord>> {
        return onRecordsRetrieved
    }

    fun getOnRootSharesNeeded(): LiveData<Void> {
        return onRootSharesNeeded
    }

    fun getOnFileViewRequest(): LiveData<FileData> {
        return onFileViewRequest
    }

    fun download(record: DownloadableRecord) {
        val download = downloadQueue.enqueueNewDownloadFor(record)
        record.observe(lifecycleOwner, download)
    }

    fun onRecordClick(record: DownloadableRecord) {
        if (record.type == RecordType.FOLDER) {
            folderPathStack.push(record)
            loadFilesOf(record)
        } else {
            getFileData(record)
        }
    }

    private fun loadFilesOf(record: DownloadableRecord) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        val archiveNr = record.archiveNr
        val folderLinkId = record.folderLinkId
        if (archiveNr != null && folderLinkId != null) {
            isBusy.value = true
            fileRepository.getChildRecordsOf(archiveNr, folderLinkId,
                SortType.NAME_ASCENDING.toBackendString(),
                object : IFileRepository.IOnRecordsRetrievedListener {
                    override fun onSuccess(recordVOs: List<RecordVO>?) {
                        isBusy.value = false
                        isRoot.value = false
                        folderName.value = record.displayName
                        existsShares.value = !recordVOs.isNullOrEmpty()
                        recordVOs?.let { onRecordsRetrieved.value = getDownloadableRecords(recordVOs) }
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        showMessage.value = error
                    }
                })
        }
    }

    private fun getFileData(record: DownloadableRecord) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        val folderLinkId = record.folderLinkId
        val archiveNr = record.archiveNr
        val archiveId = record.archiveId
        val recordId = record.recordId

        if (folderLinkId != null && archiveNr != null && archiveId != null && recordId != null) {
            isBusy.value = true
            fileRepository.getRecord(folderLinkId, archiveNr, archiveId, recordId
            ).enqueue(object : Callback<ResponseVO> {

                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    isBusy.value = false
                    val fileData = response.body()?.getFileData()
                    if (fileData != null) {
                        val file = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            fileData.fileName
                        )
                        if (fileData.contentType?.contains(FileType.PDF.toString()) == true) {
                            val intent = if (file.exists()) {
                                PdfViewerActivity.launchPdfFromPath(
                                    appContext,
                                    file.path,
                                    fileData.displayName,
                                    "",
                                    enableDownload = false)
                            } else {
                                PdfViewerActivity.launchPdfFromUrl(
                                    appContext,
                                    fileData.fileURL,
                                    fileData.displayName,
                                    "",
                                    enableDownload = false)
                            }
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            appContext.startActivity(intent)
                        } else {
                            onFileViewRequest.value = fileData
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    isBusy.value = false
                    showMessage.value = appContext.getString(R.string.generic_error)
                }
            })
        }
    }

    private fun getDownloadableRecords(recordVOs: List<RecordVO>): MutableList<DownloadableRecord> {
        val downloadableRecords = ArrayList<DownloadableRecord>()
        for (recordVO in recordVOs) {
            downloadableRecords.add(DownloadableRecord(recordVO))
        }
        return downloadableRecords
    }

    fun onBackBtnClick() {
        // This is the record of the current folder but we need his parent
        folderPathStack.pop()
        if (folderPathStack.isEmpty()) {
            onRootSharesNeeded.call()
        } else {
            val previousFolder = folderPathStack.pop()
            folderPathStack.push(previousFolder)
            loadFilesOf(previousFolder)
        }
    }

    override fun onFinished(download: Download) {
        showMessage.value = "Downloaded ${download.getDisplayName()}"
    }

    override fun onFinished(upload: Upload, succeeded: Boolean) { // Not needed
    }
}