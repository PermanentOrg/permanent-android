package org.permanent.permanent.viewmodels

import android.app.Application
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkInfo
import androidx.work.WorkManager
import org.permanent.permanent.Constants
import org.permanent.permanent.PermanentApplication
import org.permanent.permanent.R
import org.permanent.permanent.models.Download
import org.permanent.permanent.models.FileType
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.Upload
import org.permanent.permanent.network.models.FileData
import org.permanent.permanent.ui.myFiles.OnFinishedListener
import java.io.File

class FileViewOptionsViewModel(application: Application) : ObservableAndroidViewModel(application),
    OnFinishedListener {
    private val appContext = application.applicationContext
    private var fileData: FileData? = null
    private var record: Record? = null
    private var download: Download? = null
    private val showMessage = MutableLiveData<String>()
    private val onFileDownloaded = SingleLiveEvent<Void>()
    private val onShareInPermanentRequest = SingleLiveEvent<Void>()
    private val onShareToAnotherAppRequest = SingleLiveEvent<Void>()

    fun setRecord(record: Record?) {
        this.record = record
    }

    fun setFileData(fileData: FileData?) {
        this.fileData = fileData
    }

    fun onShareInPermanentBtnClick() {
        onShareInPermanentRequest.call()
    }

    fun onShareToAnotherAppBtnClick() {
        onShareToAnotherAppRequest.call()
    }

    fun getUriForSharing(): Uri? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            var collection: Uri =
                MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            var nameColumn = MediaStore.Downloads.DISPLAY_NAME
            var idColumn = MediaStore.Downloads._ID

            when {
                fileData?.contentType?.contains(FileType.IMAGE.toString()) == true -> {
                    collection =
                        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    nameColumn = MediaStore.Images.Media.DISPLAY_NAME
                    idColumn = MediaStore.Images.Media._ID
                }
                fileData?.contentType?.contains(FileType.VIDEO.toString()) == true -> {
                    collection =
                        MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    nameColumn = MediaStore.Video.Media.DISPLAY_NAME
                    idColumn = MediaStore.Video.Media._ID
                }
            }

            val projection = arrayOf(nameColumn, idColumn)
            appContext.contentResolver.query(
                collection, projection, null, null, null, null
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndex(idColumn)
                val nameIndex = cursor.getColumnIndex(nameColumn)

                while (cursor.moveToNext()) {
                    val fileName = cursor.getString(nameIndex)
                    if (fileName == fileData?.fileName) {
                        val fileId = cursor.getString(idIndex)
                        return Uri.parse("$collection/$fileId")
                    }
                }
                cursor.close()
                return null
            }
        } else {
            val file = File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                ), fileData?.fileName
            )
            return if (file.exists()) {
                FileProvider.getUriForFile(
                    appContext,
                    PermanentApplication.instance.packageName + Constants.FILE_PROVIDER_NAME,
                    file
                )
            } else null
        }
        return null
    }

    fun downloadFile(lifecycleOwner: LifecycleOwner) {
        record?.let { record ->
            download = Download(appContext, record, this)
            download?.getWorkRequest()?.let { WorkManager.getInstance(appContext).enqueue(it) }
            download?.observeWorkInfoOn(lifecycleOwner)
        }
    }

    fun cancelDownload() {
        download?.cancel()
    }

    override fun onFinished(download: Download, state: WorkInfo.State) {
        if (state == WorkInfo.State.SUCCEEDED) onFileDownloaded.call()
        else if (state == WorkInfo.State.FAILED)
            showMessage.value = appContext.getString(R.string.generic_error)
    }

    override fun onFinished(upload: Upload, succeeded: Boolean) {}

    override fun onFailedUpload(message: String) {}

    override fun onQuotaExceeded() {}

    fun getShowMessage(): LiveData<String> = showMessage

    fun getOnFileDownloaded(): LiveData<Void> = onFileDownloaded

    fun getOnShareInPermanentRequest(): MutableLiveData<Void> = onShareInPermanentRequest

    fun getOnShareToAnotherAppRequest(): MutableLiveData<Void> = onShareToAnotherAppRequest
}
