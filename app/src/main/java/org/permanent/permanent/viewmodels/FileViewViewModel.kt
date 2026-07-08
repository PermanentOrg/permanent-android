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
import org.permanent.permanent.PermanentApplication
import org.permanent.permanent.R
import org.permanent.permanent.models.Download
import org.permanent.permanent.models.FileType
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.Upload
import org.permanent.permanent.network.ConnectivityMonitorImpl
import org.permanent.permanent.network.DebugForcedOfflineMonitor
import org.permanent.permanent.network.IConnectivityMonitor
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.FileData
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.OnPreviewErrorListener
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.fileView.ImageViewUiState
import org.permanent.permanent.ui.fileView.PreviewErrorState
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
    private val _isThumbnailFailed = MutableStateFlow(false)
    val isThumbnailFailed: StateFlow<Boolean> = _isThumbnailFailed

    // Bumped on each retry so the compose viewer builds a fresh (non-equal) ImageRequest;
    // Coil caches only successes, so a same-URL retry genuinely re-fetches.
    private val _fullResRetryNonce = MutableStateFlow(0)
    val fullResRetryNonce: StateFlow<Int> = _fullResRetryNonce
    // Failure/offline card of the non-image previews (video/PDF/docs) — same visual
    // treatment as the image S6/S7 states, driven by this simpler machine.
    private val _previewErrorState = MutableStateFlow(PreviewErrorState.NONE)
    val previewErrorState: StateFlow<PreviewErrorState> = _previewErrorState
    private var loaderTimerJob: Job? = null
    private var errorGateJob: Job? = null
    private var connectivityJob: Job? = null
    private var progressiveFlowStartMs = 0L
    private var isSharpScheduled = false
    private val showMessage = MutableLiveData<String>()
    val isBusy = MutableLiveData(false)
    private val prefsHelper = PreferencesHelper(
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    internal var fileRepository: IFileRepository = FileRepositoryImpl(application)
    internal var connectivityMonitor: IConnectivityMonitor =
        (application as? PermanentApplication)?.connectivityMonitor
            ?: ConnectivityMonitorImpl(application)

    init {
        if (BuildConfig.DEBUG && DEBUG_FORCE_OFFLINE) {
            connectivityMonitor = DebugForcedOfflineMonitor(
                viewModelScope, DEBUG_RESTORE_CONNECTIVITY_AFTER_MS, connectivityMonitor
            )
        }
    }

    fun setRecord(record: Record) {
        this.record = record
        observeConnectivity()
        if (record.isImage()) {
            startProgressiveImageFlow(record.thumbnail256 ?: record.thumbURL200)
            if (!connectivityMonitor.isConnected) {
                // Offline at open (S7): show the card right away and start no network
                // loads — the thumbnail may still render from Picasso's cache.
                loaderTimerJob?.cancel()
                _imageViewUiState.value = ImageViewUiState.Offline
                return
            }
        } else if (!connectivityMonitor.isConnected) {
            _previewErrorState.value = PreviewErrorState.OFFLINE
            return
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
        // The debug hook simulates a record with no thumbnail (S5)
        val effectiveUrl = if (BuildConfig.DEBUG && DEBUG_FORCE_THUMBNAIL_ERROR) {
            null
        } else {
            thumbnailUrl
        }
        _thumbnailUrl.value = effectiveUrl
        progressiveFlowStartMs = SystemClock.elapsedRealtime()
        if (effectiveUrl == null) {
            // S5 (no thumbnail): skeleton fill with the loader from the very beginning —
            // the 500 ms window only exists to keep the blurred thumbnail undisturbed.
            _imageViewUiState.value = ImageViewUiState.BlurredThumbnailWithLoader
            return
        }
        loaderTimerJob = viewModelScope.launch {
            delay(S1_MIN_DURATION_MS)
            if (_imageViewUiState.value == ImageViewUiState.BlurredThumbnail) {
                _imageViewUiState.value = ImageViewUiState.BlurredThumbnailWithLoader
            }
        }
    }

    /**
     * Called by the UI when the thumbnail request itself failed (S5). The skeleton replaces
     * the blurred thumbnail as the background layer, and the loader shows immediately.
     */
    fun onThumbnailFailed() {
        _isThumbnailFailed.value = true
        if (_imageViewUiState.value == ImageViewUiState.BlurredThumbnail) {
            loaderTimerJob?.cancel()
            _imageViewUiState.value = ImageViewUiState.BlurredThumbnailWithLoader
        }
    }

    /**
     * Publishes the full-resolution source the compose viewer loads. The debug-only delay
     * simulates a slow network so S2 (loader) and S3 (cross-dissolve) become observable.
     */
    private fun publishFullResSource(source: Any?) {
        if (source == null) {
            // No fileURL on the record (e.g. upload still processing) — without a source
            // the Coil request never starts and its error listener can never fire
            onFullResFailed()
            return
        }
        viewModelScope.launch {
            if (BuildConfig.DEBUG && DEBUG_FULL_RES_EXTRA_DELAY_MS > 0) {
                delay(DEBUG_FULL_RES_EXTRA_DELAY_MS)
            }
            _fullResImageSource.value = if (BuildConfig.DEBUG && DEBUG_FORCE_FULL_RES_ERROR) {
                DEBUG_UNREACHABLE_URL // Coil fails realistically -> S6, tap-retry loop
            } else {
                source
            }
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

    /**
     * Called when the full-res image (or the record fetch it depends on) failed. Per spec,
     * S1 remains the only state for the first 500 ms, so an early failure waits out the
     * window — mirroring [onFullResReady] — before showing the card. Classification happens
     * at failure time: connected -> S6 (LoadFailed), otherwise -> S7 (Offline).
     */
    fun onFullResFailed() {
        if (isSharpScheduled) return
        val current = _imageViewUiState.value
        if (current == ImageViewUiState.Sharp || current == ImageViewUiState.LoadFailed
            || current == ImageViewUiState.Offline
        ) return
        loaderTimerJob?.cancel()
        val elapsedMs = SystemClock.elapsedRealtime() - progressiveFlowStartMs
        errorGateJob = viewModelScope.launch {
            delay(S1_MIN_DURATION_MS - elapsedMs) // no-op when the window has already passed
            if (!isSharpScheduled) {
                _imageViewUiState.value = if (connectivityMonitor.isConnected) {
                    ImageViewUiState.LoadFailed
                } else {
                    ImageViewUiState.Offline
                }
            }
        }
    }

    /**
     * Tap on the S6/S7 card. While still offline the tap is a no-op (the card's press
     * feedback is UI-side); otherwise retry — unlimited times, per spec no counter.
     */
    fun onErrorCardTapped() {
        val current = _imageViewUiState.value
        if (current != ImageViewUiState.LoadFailed && current != ImageViewUiState.Offline) return
        if (!connectivityMonitor.isConnected) return
        retryFullRes()
    }

    /**
     * S8: back to the loader immediately (the 500 ms window only protects first loads).
     * When the record fetch never succeeded the retry re-runs it; otherwise the nonce
     * restarts the image request.
     */
    private fun retryFullRes() {
        isSharpScheduled = false
        errorGateJob?.cancel()
        _imageViewUiState.value = ImageViewUiState.BlurredThumbnailWithLoader
        if (_fullResImageSource.value == null) {
            requestFileData()
        } else {
            _fullResRetryNonce.value++
        }
    }

    /**
     * Auto-retries when connectivity returns while sitting in an offline card (S7), and
     * reclassifies a failure card (S6) to the offline card when the loss notification
     * arrives late — the OS often reports it seconds after requests already failed.
     */
    private fun observeConnectivity() {
        if (connectivityJob != null) return
        connectivityJob = viewModelScope.launch {
            connectivityMonitor.isOnline.collect { online ->
                if (online) {
                    if (_imageViewUiState.value == ImageViewUiState.Offline) retryFullRes()
                    if (_previewErrorState.value == PreviewErrorState.OFFLINE) retryPreview()
                } else {
                    if (_imageViewUiState.value == ImageViewUiState.LoadFailed) {
                        _imageViewUiState.value = ImageViewUiState.Offline
                    }
                    if (_previewErrorState.value == PreviewErrorState.FAILED) {
                        _previewErrorState.value = PreviewErrorState.OFFLINE
                    }
                }
            }
        }
    }

    /**
     * Called when a non-image preview failed to load (record fetch, PDF stream/render).
     * Same classification as the image path: connected -> failed card, else offline card.
     */
    fun onPreviewLoadFailed() {
        if (isImage.value == true) return
        _previewErrorState.value = if (connectivityMonitor.isConnected) {
            PreviewErrorState.FAILED
        } else {
            PreviewErrorState.OFFLINE
        }
    }

    /** Bound to app:onPreviewError of the WebView (data binding can't SAM-convert
     *  an XML lambda for a multi-attribute adapter, so the listener is exposed typed). */
    val previewErrorListener = OnPreviewErrorListener { onPreviewLoadFailed() }

    /** Tap on the non-image failure/offline card; inert while still offline. */
    fun onPreviewCardTapped() {
        if (_previewErrorState.value == PreviewErrorState.NONE) return
        if (!connectivityMonitor.isConnected) return
        retryPreview()
    }

    private fun retryPreview() {
        _previewErrorState.value = PreviewErrorState.NONE
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
                    val parsedData = response.body()?.getFileData()
                    if (parsedData == null) {
                        // HTTP error statuses and unparseable payloads land here, not in
                        // onFailure — without this the viewer stalls on the loader forever
                        if (isImage.value == true) onFullResFailed() else onPreviewLoadFailed()
                        return
                    }
                    _previewErrorState.value = PreviewErrorState.NONE
                    fileData.value = parsedData
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
                                    // canRead(), not exists(): scoped storage can list a
                                    // Downloads file yet deny opening it (EACCES)
                                    if (externalFile.canRead()) {
                                        clearCache(getApplication())
                                        externalFile.copyTo(cacheFile, overwrite = true)
                                        cacheFile
                                    } else {
                                        data.fileURL
                                    }
                                )
                            } else if (externalFile.canRead()) {
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
                    if (isImage.value == true) {
                        // The full-res image can't even start without the record data, so
                        // this failure feeds the same S6/S7 classification.
                        onFullResFailed()
                    } else {
                        onPreviewLoadFailed()
                    }
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

    fun getFileData(): MutableLiveData<FileData> = fileData

    fun getFilePath(): MutableLiveData<String> = filePath

    fun getIsVideo(): MutableLiveData<Boolean> = isVideo

    fun getShowMessage(): LiveData<String> = showMessage

    fun getIsPDF(): MutableLiveData<Boolean> = isPDF

    companion object {
        // Per spec, the blurred thumbnail is the only state for the first 500 ms: the loader
        // appears only after this window, and an early full-res arrival waits it out
        private const val S1_MIN_DURATION_MS = 500L

        // Manual-test hook (debug builds only): set to e.g. 3000L to keep the full-res
        // image artificially slow so S2/S3 can be observed deterministically
        private const val DEBUG_FULL_RES_EXTRA_DELAY_MS = 0L

        // Manual-test hooks for the error/offline states (debug builds only, VSP-1754):
        // - DEBUG_FORCE_OFFLINE opens every preview in the offline state (S7); set
        //   DEBUG_RESTORE_CONNECTIVITY_AFTER_MS to e.g. 5000L to watch the auto-retry
        //   (equivalent of iOS --forceOffline / --restoreConnectivityAfter=N)
        // - DEBUG_FORCE_FULL_RES_ERROR fails the full-res request (S6 + tap-retry loop)
        // - DEBUG_FORCE_THUMBNAIL_ERROR treats every record as having no thumbnail
        //   (S5: skeleton + loader immediately); pair with DEBUG_FULL_RES_EXTRA_DELAY_MS
        //   to keep the skeleton on screen long enough to observe. The failed-request
        //   variant of S5 is best tested with a real record whose thumbnail URL 404s
        private const val DEBUG_FORCE_OFFLINE = false
        private const val DEBUG_RESTORE_CONNECTIVITY_AFTER_MS = 0L
        private const val DEBUG_FORCE_FULL_RES_ERROR = false
        private const val DEBUG_FORCE_THUMBNAIL_ERROR = false
        private const val DEBUG_UNREACHABLE_URL = "https://invalid.permanent.test/missing"
    }
}