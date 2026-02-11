package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkInfo
import androidx.work.WorkManager
import org.permanent.permanent.R
import org.permanent.permanent.models.Download
import org.permanent.permanent.models.FileType
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.Upload
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.FileData
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.myFiles.ModificationType
import org.permanent.permanent.ui.myFiles.OnFinishedListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class FileViewViewModel(application: Application) : ObservableAndroidViewModel(application),
    OnFinishedListener {
    private val appContext = application.applicationContext
    private lateinit var record: Record
    private var fileData = MutableLiveData<FileData>()
    private val filePath = MutableLiveData<String>()
    private val isVideo = MutableLiveData<Boolean>()
    private val isPDF = MutableLiveData<Boolean>()
    private val isError = MutableLiveData(false)
    private val showMessage = MutableLiveData<String>()
    val isBusy = MutableLiveData(false)
    private val prefsHelper = PreferencesHelper(
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)

    fun setRecord(record: Record) {
        this.record = record
        requestFileData()
    }

    private fun requestFileData() {
        val folderLinkId = record.folderLinkId
        val recordId = record.recordId

        if (folderLinkId != null && recordId != null) {
            isBusy.value = true
            fileRepository.getRecord(folderLinkId, recordId).enqueue(object : Callback<ResponseVO> {

                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    isBusy.value = false
                    isError.value = false
                    fileData.value = response.body()?.getFileData()
                    fileData.value?.let { data ->
                        isPDF.value = data.contentType?.contains(FileType.PDF.toString())
                        isVideo.value = data.contentType?.contains(FileType.VIDEO.toString())

                        val externalFile = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            data.fileName
                        )

                        val cacheFile = File(
                            getApplication<Application>().cacheDir,
                            data.fileName
                        )

                        try {
                            if (externalFile.exists()) {
                                clearCache(getApplication())
                                externalFile.copyTo(cacheFile, overwrite = true)
                                filePath.value = "file://${cacheFile.absolutePath}"
                            } else if (data.contentType?.contains(FileType.IMAGE.toString()) == true) {
                                filePath.value = data.thumbURL2000
                            } else {
                                filePath.value = data.fileURL
                            }
                        } catch (e: Exception) {
                            Log.e("FileViewViewModel", "File copy failed", e)
                            filePath.value = data.fileURL // fallback
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    isBusy.value = false
                    isError.value = true
                }
            })
        } else {
            Log.e("FileViewViewModel", "folderLinkId or recordId is null")
        }
    }

    fun clearCache(context: Context) {
        try {
            val cacheDir = context.cacheDir
            if (cacheDir != null && cacheDir.isDirectory) {
                cacheDir.listFiles()?.forEach { it.deleteRecursively() }
            }
        } catch (e: Exception) {
            Log.e("FileViewViewModel", "Failed to clear cache", e)
        }
    }

    fun publishRecord(record: Record) {
        val folderLinkId = prefsHelper.getPublicRecordFolderLinkId()

        if (folderLinkId != 0) {
            isBusy.value = true
            fileRepository.relocateRecords(
                mutableListOf(record),
                folderLinkId,
                ModificationType.PUBLISH,
                object : IResponseListener {
                    override fun onSuccess(message: String?) {
                        isBusy.value = false
                        message?.let { showMessage.value = it }
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        error?.let { showMessage.value = it }
                    }
                })
        }
    }

    fun download(record: Record, lifecycleOwner: LifecycleOwner) {
        val download = Download(context = appContext, record = record, listener = this)
        download.getWorkRequest()?.let { WorkManager.getInstance(appContext).enqueue(it) }
        download.observeWorkInfoOn(lifecycleOwner)
        isBusy.value = true
    }

    override fun onFinished(upload: Upload, succeeded: Boolean) {}

    override fun onFinished(download: Download, state: WorkInfo.State) {
        isBusy.value = false
        if (state == WorkInfo.State.SUCCEEDED) showMessage.value =
            appContext.getString(R.string.download_complete)
        else if (state == WorkInfo.State.FAILED)
            showMessage.value = appContext.getString(R.string.generic_error)
    }

    override fun onFailedUpload(message: String) {}

    override fun onQuotaExceeded() {}

    fun onRetryBtnClick() {
        requestFileData()
    }

    fun getFileData(): MutableLiveData<FileData> = fileData

    fun getFilePath(): MutableLiveData<String> = filePath

    fun getIsVideo(): MutableLiveData<Boolean> = isVideo

    fun getShowMessage(): LiveData<String> = showMessage

    fun getIsPDF(): MutableLiveData<Boolean> = isPDF

    fun getIsError(): MutableLiveData<Boolean> = isError
}