package org.permanent.permanent.viewmodels

import android.app.Application
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
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.ShareRequestType
import org.permanent.permanent.network.models.FileData
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.network.models.Shareby_urlVO
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.repositories.IShareRepository
import org.permanent.permanent.repositories.ShareRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.Workspace
import org.permanent.permanent.ui.myFiles.OnFinishedListener
import org.permanent.permanent.ui.myFiles.RelocationType
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class RecordOptionsViewModel(application: Application) : ObservableAndroidViewModel(application),
    OnFinishedListener {

    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val isBusy = MutableLiveData(false)
    private val showSnackbar = MutableLiveData<String>()
    private val showSnackbarSuccess = MutableLiveData<String>()
    private lateinit var record: Record
    private lateinit var actualAccessRole: AccessRole
    private lateinit var workspace: Workspace
    private val isFragmentShownInSharedWithMe = MutableLiveData(false)
    private val isFragmentShownInRootFolder = MutableLiveData(false)
    private var fileData: FileData? = null
    private var download: Download? = null
    private val recordName = MutableLiveData<String>()
    private val allSharesSize = MutableLiveData(0)
    private val showViewAllBtn = MutableLiveData(false)
    private val recordPermission = MutableLiveData<String>()
    private val sharedWithLabelTxt = MutableLiveData<String>()
    private var shareByUrlVO: Shareby_urlVO? = null
    private val shareLink = MutableLiveData("")
    private val hiddenOptions = MutableLiveData<MutableList<RecordOption>>(mutableListOf())
    private val allShares = mutableListOf<Share>()
    private val onSharesRetrieved = SingleLiveEvent<MutableList<Share>>()
    private val onRequestWritePermission = SingleLiveEvent<Void>()
    private val onFileDownloadRequest = SingleLiveEvent<Void>()
    private val onShareLinkRequest = SingleLiveEvent<String>()
    private val onDeleteRequest = SingleLiveEvent<Void>()
    private val onLeaveShareRequest = SingleLiveEvent<Void>()
    private val onRenameRequest = SingleLiveEvent<Void>()
    private val onManageSharingRequest = SingleLiveEvent<Void>()
    private val onShareToAnotherAppRequest = SingleLiveEvent<String>()
    private val onFileDownloadedForSharing = SingleLiveEvent<String>()
    private val onRelocateRequest = MutableLiveData<RelocationType>()
    private val onPublishRequest = SingleLiveEvent<Void>()
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)
    private var shareRepository: IShareRepository = ShareRepositoryImpl(appContext)

    fun initWith(
        record: Record,
        workspace: Workspace,
        isShownInSharedWithMe: Boolean,
        isShownInRootFolder: Boolean
    ) {
        this.record = record
        this.workspace = workspace
        this.isFragmentShownInSharedWithMe.value = isShownInSharedWithMe
        this.isFragmentShownInRootFolder.value = isShownInRootFolder
        recordName.value = record.displayName
        actualAccessRole =
            record.accessRole?.getInferior(CurrentArchivePermissionsManager.instance.getAccessRole())
                ?: AccessRole.VIEWER
        recordPermission.value = actualAccessRole.toTitleCase()
        initShares(record)
        updateSharedWithBtnTxt(allShares.size)
        if (workspace == Workspace.PRIVATE_FILES) {
            checkForExistingLink(record)
        } else {
            shareLink.value =
                if (record.type == RecordType.FILE) BuildConfig.BASE_URL + "p/archive/" +
                        prefsHelper.getCurrentArchiveNr() + "/" + record.parentFolderArchiveNr + "/" +
                        record.parentFolderLinkId + "/record/" + record.archiveNr
                else BuildConfig.BASE_URL + "p/archive/" + prefsHelper.getCurrentArchiveNr() + "/" +
                        record.archiveNr + "/" + record.folderLinkId
        }

        if (record.type == RecordType.FOLDER) {
            hiddenOptions.value?.add(RecordOption.DOWNLOAD)
            hiddenOptions.value?.add(RecordOption.SHARE_TO_ANOTHER_APP)
        }
        if (workspace == Workspace.PRIVATE_FILES) {
            hiddenOptions.value?.add(RecordOption.LEAVE_SHARE)
            if (!CurrentArchivePermissionsManager.instance.isCreateAvailable())
                hiddenOptions.value?.add(RecordOption.COPY)
            if (!CurrentArchivePermissionsManager.instance.isDeleteAvailable())
                hiddenOptions.value?.add(RecordOption.DELETE)
            if (!CurrentArchivePermissionsManager.instance.isMoveAvailable())
                hiddenOptions.value?.add(RecordOption.MOVE)
            if (!CurrentArchivePermissionsManager.instance.isEditAvailable())
                hiddenOptions.value?.add(RecordOption.RENAME)
            if (!CurrentArchivePermissionsManager.instance.isOwnershipAvailable())
                hiddenOptions.value?.add(RecordOption.SHARE_VIA_PERMANENT)
            if (!CurrentArchivePermissionsManager.instance.isPublishAvailable())
                hiddenOptions.value?.add(RecordOption.PUBLISH)
        } else if (workspace == Workspace.PUBLIC_FILES) {
            hiddenOptions.value?.add(RecordOption.PUBLISH)
            hiddenOptions.value?.add(RecordOption.SHARE_VIA_PERMANENT)
            hiddenOptions.value?.add(RecordOption.LEAVE_SHARE)
            if (!CurrentArchivePermissionsManager.instance.isCreateAvailable())
                hiddenOptions.value?.add(RecordOption.COPY)
            if (!CurrentArchivePermissionsManager.instance.isDeleteAvailable())
                hiddenOptions.value?.add(RecordOption.DELETE)
            if (!CurrentArchivePermissionsManager.instance.isMoveAvailable())
                hiddenOptions.value?.add(RecordOption.MOVE)
            if (!CurrentArchivePermissionsManager.instance.isEditAvailable())
                hiddenOptions.value?.add(RecordOption.RENAME)
        } else if (workspace == Workspace.SHARES) {
            hiddenOptions.value?.add(RecordOption.PUBLISH)
            hiddenOptions.value?.add(RecordOption.COPY_LINK)
            hiddenOptions.value?.add(RecordOption.SHARE_TO_ANOTHER_APP)
            if (!actualAccessRole.isOwnershipAvailable() || isFragmentShownInSharedWithMe.value == true) {
                hiddenOptions.value?.add(RecordOption.SHARE_VIA_PERMANENT)
            }
            if (!actualAccessRole.isCreateAvailable() || isFragmentShownInRootFolder.value == true) {
                hiddenOptions.value?.add(RecordOption.COPY)
            }
            if (!actualAccessRole.isMoveAvailable() || isFragmentShownInRootFolder.value == true) {
                hiddenOptions.value?.add(RecordOption.MOVE)
            }
            if (!actualAccessRole.isDeleteAvailable() || isFragmentShownInSharedWithMe.value == true && isFragmentShownInRootFolder.value == true) {
                hiddenOptions.value?.add(RecordOption.DELETE)
            }
            if (isFragmentShownInSharedWithMe.value == false || isFragmentShownInRootFolder.value == false) {
                hiddenOptions.value?.add(RecordOption.LEAVE_SHARE)
            }
            if (!actualAccessRole.isEditAvailable())
                hiddenOptions.value?.add(RecordOption.RENAME)
        } else { // Public Archive
            hiddenOptions.value?.add(RecordOption.PUBLISH)
            hiddenOptions.value?.add(RecordOption.DOWNLOAD)
            hiddenOptions.value?.add(RecordOption.DELETE)
            hiddenOptions.value?.add(RecordOption.MOVE)
            hiddenOptions.value?.add(RecordOption.SHARE_VIA_PERMANENT)
            hiddenOptions.value?.add(RecordOption.COPY)
            hiddenOptions.value?.add(RecordOption.RENAME)
            hiddenOptions.value?.add(RecordOption.LEAVE_SHARE)
        }
    }

    private fun updateSharedWithBtnTxt(sharesSize: Int?) {
        sharesSize?.let {
            sharedWithLabelTxt.value = appContext.getString(R.string.record_options_shared_with, it)
        }
    }

    private fun initShares(record: Record?) {
        record?.shares?.let {
            allShares.addAll(it)
            allSharesSize.value = allShares.size

            if (allShares.size != 0) updateSharesUI()
        }
    }

    private fun updateSharesUI() {
        val fewShares = mutableListOf<Share>()

        if (allShares.size > 0) fewShares.add(allShares[0])
        if (allShares.size > 1) fewShares.add(allShares[1])
        onSharesRetrieved.value = fewShares
        showViewAllBtn.value = allShares.size > DEFAULT_NR_OF_VISIBLE_SHARES
    }

    fun onDownloadBtnClick() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
            && !DevicePermissionsHelper().hasWriteStoragePermission(appContext)
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
        onPublishRequest.call()
    }

    fun onShareLinkBtnClick() {
        onShareLinkRequest.value = shareLink.value.toString()
    }

    fun onViewAllBtnClick() {
        onSharesRetrieved.value = allShares
        showViewAllBtn.value = false
    }

    fun onDeleteBtnClick() {
        onDeleteRequest.call()
    }

    fun onLeaveShareBtnClick() {
        onLeaveShareRequest.call()
    }

    fun onRenameBtnClick() {
        onRenameRequest.call()
    }

    private fun checkForExistingLink(record: Record) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        shareRepository.requestShareLink(record, ShareRequestType.GET,
            object : IShareRepository.IShareByUrlListener {
                override fun onSuccess(shareByUrlVO: Shareby_urlVO?) {
                    isBusy.value = false
                    this@RecordOptionsViewModel.shareByUrlVO = shareByUrlVO
                    shareByUrlVO?.shareUrl?.let { shareLink.value = it }
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                }
            })
    }

    fun onManageSharingBtnClick() {
        onManageSharingRequest.call()
    }

    fun publishRecord() {
        val folderLinkId = prefsHelper.getPublicRecordFolderLinkId()

        if (folderLinkId != 0) {
            isBusy.value = true
            fileRepository.relocateRecords(mutableListOf(record),
                folderLinkId,
                RelocationType.PUBLISH,
                object : IResponseListener {
                    override fun onSuccess(message: String?) {
                        isBusy.value = false
                        message?.let { showSnackbarSuccess.value = it }
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        error?.let { showSnackbar.value = it }
                    }
                })
        }
    }

    fun onShareToAnotherAppBtnClick() {
        // Requesting FileData
        val folderLinkId = record.folderLinkId
        val recordId = record.recordId

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
                    showSnackbar.value = t.message
                }
            })
        }
    }

    fun downloadFileForSharing(lifecycleOwner: LifecycleOwner) {
        download = Download(appContext, record, this)
        download?.getWorkRequest()?.let { WorkManager.getInstance(appContext).enqueue(it) }
        download?.observeWorkInfoOn(lifecycleOwner)
    }

    fun cancelDownload() {
        download?.cancel()
    }

    override fun onFinished(download: Download, state: WorkInfo.State) {
        if (state == WorkInfo.State.SUCCEEDED) onFileDownloadedForSharing.value =
            fileData?.contentType
        else if (state == WorkInfo.State.FAILED)
            showSnackbar.value = appContext.getString(R.string.generic_error)
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

    fun getShowSnackbar(): LiveData<String> = showSnackbar

    fun getShowSnackbarSuccess(): LiveData<String> = showSnackbarSuccess

    fun getOnFileDownloadedForSharing(): LiveData<String> = onFileDownloadedForSharing

    fun getRecord(): Record = record

    fun getWorkspace(): Workspace = workspace

    fun getIsFragmentShownInSharedWithMe(): MutableLiveData<Boolean> = isFragmentShownInSharedWithMe

    fun getIsFragmentShownInRootFolder(): MutableLiveData<Boolean> = isFragmentShownInRootFolder

    fun getRecordName(): MutableLiveData<String> = recordName

    fun getSharesSize(): MutableLiveData<Int> = allSharesSize

    fun getShowViewAllBtn(): MutableLiveData<Boolean> = showViewAllBtn

    fun getRecordPermission(): MutableLiveData<String> = recordPermission

    fun getSharedWithLabelTxt(): MutableLiveData<String> = sharedWithLabelTxt

    fun getShareByUrlVO(): Shareby_urlVO? = shareByUrlVO

    fun getShareLink(): MutableLiveData<String> = shareLink

    fun getHiddenOptions(): MutableLiveData<MutableList<RecordOption>> = hiddenOptions

    fun getOnSharesRetrieved(): MutableLiveData<MutableList<Share>> = onSharesRetrieved

    fun getOnRequestWritePermission(): MutableLiveData<Void> = onRequestWritePermission

    fun getOnFileDownloadRequest(): MutableLiveData<Void> = onFileDownloadRequest

    fun getOnRelocateRequest(): MutableLiveData<RelocationType> = onRelocateRequest

    fun getOnPublishRequest(): MutableLiveData<Void> = onPublishRequest

    fun getOnShareLinkRequest(): MutableLiveData<String> = onShareLinkRequest

    fun getOnDeleteRequest(): MutableLiveData<Void> = onDeleteRequest

    fun getOnLeaveShareRequest(): MutableLiveData<Void> = onLeaveShareRequest

    fun getOnRenameRequest(): MutableLiveData<Void> = onRenameRequest

    fun getOnManageSharingRequest(): MutableLiveData<Void> = onManageSharingRequest

    fun getOnShareToAnotherAppRequest(): MutableLiveData<String> = onShareToAnotherAppRequest

    companion object {
        const val DEFAULT_NR_OF_VISIBLE_SHARES = 2
    }
}
