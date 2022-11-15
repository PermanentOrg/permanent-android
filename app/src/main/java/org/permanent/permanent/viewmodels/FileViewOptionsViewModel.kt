package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.myFiles.OnFinishedListener
import java.io.File

class FileViewOptionsViewModel(application: Application) : ObservableAndroidViewModel(application),
    OnFinishedListener {
    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private var fileData: FileData? = null
    private var record: Record? = null
    private var download: Download? = null
    private val showMessage = MutableLiveData<String>()
    private val shouldHideCopyLinkButton = MutableLiveData(false)
    private val shouldHideShareViaPermanentButton = MutableLiveData(false)
    private val shouldHideShareToAnotherAppButton = MutableLiveData(false)
    private val onFileDownloaded = SingleLiveEvent<Void>()
    private val onShareViaPermanentRequest = SingleLiveEvent<Void>()
    private val onShareToAnotherAppRequest = SingleLiveEvent<Void>()

    fun setArguments(record: Record?, fileData: FileData?) {
        this.record = record
        shouldHideCopyLinkButton.value = record?.parentFolderArchiveNr == null
        fileData?.let {
            this.fileData = fileData
            shouldHideShareViaPermanentButton.value = it.accessRole != AccessRole.OWNER ||
                    !CurrentArchivePermissionsManager.instance.isOwnershipAvailable() ||
                    record?.parentFolderArchiveNr != null
        }
        shouldHideShareToAnotherAppButton.value = record?.type == RecordType.FOLDER
    }

    fun onCopyLinkBtnClick() {
        val sharableLink =
            if (record?.type == RecordType.FILE) BuildConfig.BASE_URL + "p/archive/" +
                    prefsHelper.getCurrentArchiveNr() + "/" + record?.parentFolderArchiveNr + "/" +
                    record?.parentFolderLinkId + "/record/" + record?.archiveNr
            else BuildConfig.BASE_URL + "p/archive/" + prefsHelper.getCurrentArchiveNr() + "/" +
                    record?.archiveNr + "/" + record?.folderLinkId
        val clipboard = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText(
            appContext.getString(R.string.share_management_share_link_title), sharableLink
        )
        clipboard.setPrimaryClip(clip)
        showMessage.value = appContext.getString(R.string.share_management_link_copied)
    }

    fun onShareViaPermanentBtnClick() {
        onShareViaPermanentRequest.call()
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

    fun getShouldHideCopyLinkButton(): LiveData<Boolean> =
        shouldHideCopyLinkButton

    fun getShouldHideShareViaPermanentButton(): LiveData<Boolean> =
        shouldHideShareViaPermanentButton

    fun getShouldHideShareToAnotherAppButton(): LiveData<Boolean> =
        shouldHideShareToAnotherAppButton

    fun getOnFileDownloaded(): LiveData<Void> = onFileDownloaded

    fun getOnShareViaPermanentRequest(): MutableLiveData<Void> = onShareViaPermanentRequest

    fun getOnShareToAnotherAppRequest(): MutableLiveData<Void> = onShareToAnotherAppRequest
}
