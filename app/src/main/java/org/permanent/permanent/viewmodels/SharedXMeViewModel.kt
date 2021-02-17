package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Download
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.models.Upload
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.myFiles.OnFinishedListener
import org.permanent.permanent.ui.myFiles.SortType
import org.permanent.permanent.ui.myFiles.download.DownloadQueue
import org.permanent.permanent.ui.myFiles.download.DownloadableRecord
import java.util.*

class SharedXMeViewModel(application: Application
) : ObservableAndroidViewModel(application), OnFinishedListener {

    private lateinit var lifecycleOwner: LifecycleOwner
    private lateinit var downloadQueue: DownloadQueue
    private val isBusy = MutableLiveData(false)
    private val showMessage = MutableLiveData<String>()
    var existsShares = MutableLiveData(false)
    private var folderPathStack: Stack<Record> = Stack()
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)

    fun setLifecycleOwner(lifecycleOwner: LifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner
        downloadQueue = DownloadQueue(getApplication(), lifecycleOwner, this)
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getShowMessage(): LiveData<String> {
        return showMessage
    }

    fun download(record: DownloadableRecord) {
        val download = downloadQueue.enqueueNewDownloadFor(record)
        record.observe(lifecycleOwner, download)
    }

    fun onRecordClick(record: DownloadableRecord) {
        if (record.type == RecordType.FOLDER) {
            folderPathStack.push(record)
            loadFilesOf(record)
        }
    }

    private fun loadFilesOf(record: DownloadableRecord) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        record.archiveNr?.let {
            isBusy.value = true
            fileRepository.getChildRecordsOf(it, SortType.NAME_ASCENDING.toBackendString(),
                object : IFileRepository.IOnRecordsRetrievedListener {
                    override fun onSuccess(records: List<Record>?) {
                        isBusy.value = false
//                        val parentName = folder.getDisplayName()
//                        folderName.value = parentName
//                        isRoot.value = parentName.equals(Constants.MY_FILES_FOLDER)
//
//                        if (records != null) {
//                            existsFiles.value = records.isNotEmpty()
//                            onFilesRetrieved.value = records
//                        }
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        showMessage.value = error
                    }
                })
        }
    }

    override fun onFinished(download: Download) {
        showMessage.value = "Downloaded ${download.getDisplayName()}"
    }

    override fun onFinished(upload: Upload, succeeded: Boolean) { // Not needed
    }
}