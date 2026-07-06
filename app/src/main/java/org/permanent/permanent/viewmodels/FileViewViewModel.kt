package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import android.os.Environment
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.permanent.permanent.BuildConfig
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
import org.permanent.permanent.ui.fileView.ImageViewUiState
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
    val isImage = MutableLiveData(false)
    private val _imageViewUiState =
        MutableStateFlow<ImageViewUiState>(ImageViewUiState.BlurredThumbnail)
    val imageViewUiState: StateFlow<ImageViewUiState> = _imageViewUiState
    private val _fullResImageSource = MutableStateFlow<Any?>(null)
    val fullResImageSource: StateFlow<Any?> = _fullResImageSource
    private val _thumbnailUrl = MutableStateFlow<String?>(null)
    val thumbnailUrl: StateFlow<String?> = _thumbnailUrl
    private var loaderTimerJob: Job? = null
    private var progressiveFlowStartMs = 0L
    private var isSharpScheduled = false
    private val isError = MutableLiveData(false)
    private val showMessage = MutableLiveData<String>()
    val isBusy = MutableLiveData(false)
    private val prefsHelper = PreferencesHelper(
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)

    fun setRecord(record: Record) {
        this.record = record
        if (record.isImage()) {
            startProgressiveImageFlow(record.thumbnail256 ?: record.thumbURL200)
        }
        requestFileData()
    }

    /**
     * Enters S1 (blurred thumbnail) and starts the 500 ms timer that promotes the state
     * to S2 (loader visible) unless the full-res image arrives first. Idempotent.
     */
    private fun startProgressiveImageFlow(thumbnailUrl: String?) {
        if (isImage.value == true) return
        isImage.value = true
        _thumbnailUrl.value = thumbnailUrl
        progressiveFlowStartMs = SystemClock.elapsedRealtime()
        loaderTimerJob = viewModelScope.launch {
            delay(S1_MIN_DURATION_MS)
            if (_imageViewUiState.value == ImageViewUiState.BlurredThumbnail) {
                _imageViewUiState.value = ImageViewUiState.BlurredThumbnailWithLoader
            }
        }
    }

    /**
     * Publishes the full-resolution source the compose viewer loads. The debug-only delay
     * simulates a slow network so S2 (loader) and S3 (cross-dissolve) become observable.
     */
    private fun publishFullResSource(source: Any?) {
        viewModelScope.launch {
            if (BuildConfig.DEBUG && DEBUG_FULL_RES_EXTRA_DELAY_MS > 0) {
                delay(DEBUG_FULL_RES_EXTRA_DELAY_MS)
            }
            _fullResImageSource.value = source
        }
    }

    /**
     * Called by the UI when the full-res image has been loaded and decoded. Per spec, S1
     * remains the only state for the first 500 ms regardless of network speed, so an early
     * arrival waits out the remainder of that window before dissolving to Sharp.
     */
    fun onFullResReady() {
        if (isSharpScheduled) return
        isSharpScheduled = true
        loaderTimerJob?.cancel()
        val elapsedMs = SystemClock.elapsedRealtime() - progressiveFlowStartMs
        viewModelScope.launch {
            delay(S1_MIN_DURATION_MS - elapsedMs) // no-op when the window has already passed
            _imageViewUiState.value = ImageViewUiState.Sharp
        }
    }

    fun onFullResFailed() {
        // STUB: companion error-handling ticket — surface FullResFailed/Offline states
        // (Figma S5–S8) and a retry path here.
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

                        val isImageContent =
                            data.contentType?.contains(FileType.IMAGE.toString()) == true

                        try {
                            if (isImageContent) {
                                // Safety net: activates the progressive viewer late when the
                                // record's backend type was missing at open time
                                startProgressiveImageFlow(data.thumbnail256)
                                publishFullResSource(
                                    if (externalFile.exists()) {
                                        clearCache(getApplication())
                                        externalFile.copyTo(cacheFile, overwrite = true)
                                        cacheFile
                                    } else {
                                        data.fileURL
                                    }
                                )
                            } else if (externalFile.exists()) {
                                clearCache(getApplication())
                                externalFile.copyTo(cacheFile, overwrite = true)
                                filePath.value = "file://${cacheFile.absolutePath}"
                            } else {
                                filePath.value = data.fileURL
                            }
                        } catch (e: Exception) {
                            Log.e("FileViewViewModel", "File copy failed", e)
                            if (isImageContent) {
                                publishFullResSource(data.fileURL) // fallback
                            } else {
                                filePath.value = data.fileURL // fallback
                            }
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

    companion object {
        // Per spec, the blurred thumbnail is the only state for the first 500 ms: the loader
        // appears only after this window, and an early full-res arrival waits it out
        private const val S1_MIN_DURATION_MS = 500L

        // Manual-test hook (debug builds only): set to e.g. 3000L to keep the full-res
        // image artificially slow so S2/S3 can be observed deterministically
        private const val DEBUG_FULL_RES_EXTRA_DELAY_MS = 0L
    }
}