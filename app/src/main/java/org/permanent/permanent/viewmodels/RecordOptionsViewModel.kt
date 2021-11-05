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
import org.permanent.permanent.*
import org.permanent.permanent.models.*
import org.permanent.permanent.network.models.FileData
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.myFiles.OnFinishedListener
import org.permanent.permanent.ui.myFiles.RelocationType
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class RecordOptionsViewModel(application: Application) : ObservableAndroidViewModel(application),
    OnFinishedListener {

    private val appContext = application.applicationContext
    private val isBusy = MutableLiveData(false)
    private val showMessage = MutableLiveData<String>()
    private var record: Record? = null
    private var fileData: FileData? = null
    private var download: Download? = null
    private val recordName = MutableLiveData<String>()
    private val hiddenOptions = MutableLiveData(mutableListOf(RecordOption.PUBLISH))
    private val onRequestWritePermission = SingleLiveEvent<Void>()
    private val onFileDownloadRequest = SingleLiveEvent<Void>()
    private val onDeleteRequest = SingleLiveEvent<Void>()
    private val onRenameRequest = SingleLiveEvent<Void>()
    private val onShareViaPermanentRequest = SingleLiveEvent<Void>()
    private val onShareToAnotherAppRequest = SingleLiveEvent<String>()
    private val onFileDownloadedForSharing = SingleLiveEvent<String>()
    private val onRelocateRequest = MutableLiveData<RelocationType>()
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)

    fun setRecord(record: Record?, isShownInMyFilesFragment: Boolean?) {
        this.record = record
        recordName.value = record?.displayName
        if (record?.type == RecordType.FOLDER) {
            hiddenOptions.value?.add(RecordOption.DOWNLOAD)
            hiddenOptions.value?.add(RecordOption.SHARE_TO_ANOTHER_APP)
        }
        if (isShownInMyFilesFragment == true) {
            if (!ArchivePermissionsManager.instance.isCreateAvailable())
                hiddenOptions.value?.add(RecordOption.COPY)
            if (!ArchivePermissionsManager.instance.isDeleteAvailable())
                hiddenOptions.value?.add(RecordOption.DELETE)
            if (!ArchivePermissionsManager.instance.isMoveAvailable())
                hiddenOptions.value?.add(RecordOption.MOVE)
            if (!ArchivePermissionsManager.instance.isShareAvailable()) {
                hiddenOptions.value?.add(RecordOption.SHARE_VIA_PERMANENT)
                hiddenOptions.value?.add(RecordOption.SHARE_TO_ANOTHER_APP)
            }
            if (!ArchivePermissionsManager.instance.isEditAvailable())
                hiddenOptions.value?.add(RecordOption.RENAME)
        } else {
            hiddenOptions.value?.add(RecordOption.DELETE)
            hiddenOptions.value?.add(RecordOption.MOVE)
            hiddenOptions.value?.add(RecordOption.SHARE_VIA_PERMANENT)
            hiddenOptions.value?.add(RecordOption.SHARE_TO_ANOTHER_APP)
            hiddenOptions.value?.add(RecordOption.COPY)
            hiddenOptions.value?.add(RecordOption.RENAME)
        }
    }

    fun onDownloadBtnClick() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
            && !PermissionsHelper().hasWriteStoragePermission(appContext)
        ) {
            onRequestWritePermission.call()
        } else {
            startFileDownload()
        }
    }

    fun onWritePermissionGranted() {
        startFileDownload()
    }

    private fun startFileDownload() {
        onFileDownloadRequest.call()
    }

    fun onCopyBtnClick() {
        onRelocateRequest.value = RelocationType.COPY
    }

    fun onMoveBtnClick() {
        onRelocateRequest.value = RelocationType.MOVE
    }

    fun onPublishBtnClick() {
    }

    fun onDeleteBtnClick() {
        onDeleteRequest.call()
    }

    fun onRenameBtnClick() {
        onRenameRequest.call()
    }

    fun onShareViaPermanentBtnClick() {
        onShareViaPermanentRequest.call()
    }

    fun onShareToAnotherAppBtnClick() {
        // Requesting FileData
        val folderLinkId = record?.folderLinkId
        val recordId = record?.recordId

        if (folderLinkId != null && recordId != null) {
            isBusy.value = true
            fileRepository.getRecord(folderLinkId, recordId).enqueue(object : Callback<ResponseVO> {

                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    isBusy.value = false
                    fileData = response.body()?.getFileData()
                    onShareToAnotherAppRequest.value = fileData?.contentType
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    isBusy.value = false
                    showMessage.value = t.message
                }
            })
        }
    }

    fun downloadFileForSharing(lifecycleOwner: LifecycleOwner) {
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
        if (state == WorkInfo.State.SUCCEEDED) onFileDownloadedForSharing.value =
            fileData?.contentType
        else if (state == WorkInfo.State.FAILED)
            showMessage.value = appContext.getString(R.string.generic_error)
    }

    override fun onFinished(upload: Upload, succeeded: Boolean) {}

    override fun onFailedUpload(message: String) {}

    override fun onQuotaExceeded() {}

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

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getShowMessage(): LiveData<String> = showMessage

    fun getOnFileDownloadedForSharing(): LiveData<String> = onFileDownloadedForSharing

    fun getName(): MutableLiveData<String> = recordName

    fun getHiddenOptions(): MutableLiveData<MutableList<RecordOption>> = hiddenOptions

    fun getOnRequestWritePermission(): MutableLiveData<Void> = onRequestWritePermission

    fun getOnFileDownloadRequest(): MutableLiveData<Void> = onFileDownloadRequest

    fun getOnRelocateRequest(): MutableLiveData<RelocationType> = onRelocateRequest

    fun getOnDeleteRequest(): MutableLiveData<Void> = onDeleteRequest

    fun getOnRenameRequest(): MutableLiveData<Void> = onRenameRequest

    fun getOnShareViaPermanentRequest(): MutableLiveData<Void> = onShareViaPermanentRequest

    fun getOnShareToAnotherAppRequest(): MutableLiveData<String> = onShareToAnotherAppRequest
}
