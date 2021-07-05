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
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.myFiles.OnFinishedListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class FileViewViewModel(application: Application
) : ObservableAndroidViewModel(application), OnFinishedListener {

    private val appContext = application.applicationContext
    private lateinit var record: Record
    private var file: File? = null
    private var fileData = MutableLiveData<FileData>()
    private val filePath = MutableLiveData<String>()
    private val isVideo = MutableLiveData<Boolean>()
    private val isPDF = MutableLiveData<Boolean>()
    private val showMessage = MutableLiveData<String>()
    private val onFileDownloaded = SingleLiveEvent<Void>()
    val isBusy = MutableLiveData(false)
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
                    fileData.value = response.body()?.getFileData()
                    fileData.value?.let {
                        isPDF.value = it.contentType?.contains(FileType.PDF.toString())
                        isVideo.value = it.contentType?.contains(FileType.VIDEO.toString())
                        file = File(
                            Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS
                            ), it.fileName
                        )
                        filePath.value = if (file?.exists() == true)
                            Uri.fromFile(file).toString() else it.fileURL
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    isBusy.value = false
                    showMessage.value = appContext.getString(R.string.generic_error)
                }
            })
        }
    }

    fun getUriForSharing(): Uri? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            var collection: Uri = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            var nameColumn = MediaStore.Downloads.DISPLAY_NAME
            var idColumn = MediaStore.Downloads._ID

            when {
                fileData.value?.contentType?.contains(FileType.IMAGE.toString()) == true -> {
                    collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    nameColumn = MediaStore.Images.Media.DISPLAY_NAME
                    idColumn = MediaStore.Images.Media._ID
                }
                fileData.value?.contentType?.contains(FileType.VIDEO.toString()) == true -> {
                    collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    nameColumn = MediaStore.Video.Media.DISPLAY_NAME
                    idColumn = MediaStore.Video.Media._ID
                }
            }

            val projection = arrayOf(nameColumn, idColumn)
            appContext.contentResolver.query(collection, projection, null, null, null, null
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndex(idColumn)
                val nameIndex = cursor.getColumnIndex(nameColumn)

                while (cursor.moveToNext()) {
                    val fileName = cursor.getString(nameIndex)
                    if (fileName == fileData.value?.fileName) {
                        val fileId = cursor.getString(idIndex)
                        return Uri.parse("$collection/$fileId")
                    }
                }
                cursor.close()
                return null
            }
        } else {
            return if (file?.exists() == true) {
                FileProvider.getUriForFile(
                    appContext,
                    PermanentApplication.instance.packageName + Constants.FILE_PROVIDER_NAME,
                    file!!
                )
            } else null
        }
        return null
    }

    fun downloadFile(lifecycleOwner: LifecycleOwner) {
        isBusy.value = true
        val download = Download(appContext, record, this)
        download.getWorkRequest()?.let { WorkManager.getInstance(appContext).enqueue(it) }
        download.observeWorkInfoOn(lifecycleOwner)
    }

    override fun onFinished(download: Download, state: WorkInfo.State) {
        isBusy.value = false
        if (state == WorkInfo.State.SUCCEEDED) onFileDownloaded.call()
        else if (state == WorkInfo.State.FAILED)
            showMessage.value = appContext.getString(R.string.generic_error)
    }

    override fun onFailed(message: String) {
        isBusy.value = false
        showMessage.value = message
    }

    override fun onFinished(upload: Upload, succeeded: Boolean) {} // Not needed

    override fun onQuotaExceeded() {} // Not needed

    fun getFileData(): MutableLiveData<FileData> = fileData

    fun getFilePath(): MutableLiveData<String> = filePath

    fun getIsVideo(): MutableLiveData<Boolean> = isVideo

    fun getShowMessage(): LiveData<String> = showMessage

    fun getOnFileDownloaded(): SingleLiveEvent<Void> = onFileDownloaded

    fun getIsPDFViewVisible(): MutableLiveData<Boolean> = isPDF
}