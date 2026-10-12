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
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.Constants
import org.permanent.permanent.CurrentArchivePermissionsManager
import org.permanent.permanent.DevicePermissionsHelper
import org.permanent.permanent.PermanentApplication
import org.permanent.permanent.R
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Download
import org.permanent.permanent.models.EventAction
import org.permanent.permanent.models.FileType
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.models.Upload
import org.permanent.permanent.ui.pendingInvitationCount
import org.permanent.permanent.network.models.FileData
import org.permanent.permanent.models.FileSessionData
import org.permanent.permanent.network.IFileDataListener
import org.permanent.permanent.repositories.EventsRepositoryImpl
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IEventsRepository
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.Workspace
import org.permanent.permanent.ui.bytesToHumanReadableString
import org.permanent.permanent.ui.myFiles.OnFinishedListener
import org.permanent.permanent.ui.toDisplayDate
import java.io.File

class RecordMenuViewModel(application: Application) : ObservableAndroidViewModel(application),
    OnFinishedListener {

    private val ctx = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val isTablet = prefsHelper.isTablet()
    private val _isBusyState = MutableStateFlow(false)
    val isBusyState: StateFlow<Boolean> = _isBusyState
    private val _menuItems = MutableStateFlow<List<RecordMenuItem>>(emptyList())
    val menuItems: StateFlow<List<RecordMenuItem>> = _menuItems
    private val showSnackbar = MutableLiveData<String>()
    private lateinit var record: Record
    private lateinit var actualAccessRole: AccessRole
    private lateinit var workspace: Workspace
    private var publicLink: String? = null
    private val isFragmentShownInSharedWithMe = MutableLiveData(false)
    private val isFragmentShownInRootFolder = MutableLiveData(false)
    private var fileData: FileData? = null
    private var download: Download? = null
    private val _recordThumb = MutableStateFlow("")
    val recordThumb: StateFlow<String> = _recordThumb
    private val _recordName = MutableStateFlow("")
    val recordName: StateFlow<String> = _recordName
    private val _recordSize = MutableStateFlow("")
    val recordSize: StateFlow<String> = _recordSize
    private val _recordDate = MutableStateFlow("")
    val recordDate: StateFlow<String> = _recordDate
    private val _archiveThumb = MutableStateFlow("")
    val archiveThumb: StateFlow<String> = _archiveThumb
    private val _archiveName = MutableStateFlow("")
    val archiveName: StateFlow<String> = _archiveName
    private val _accessRole = MutableStateFlow(AccessRole.VIEWER)
    val accessRole: StateFlow<AccessRole> = _accessRole
    val pendingInvitationCount: Int
        get() = record.pendingInvitationCount
    private val onRequestWritePermission = SingleLiveEvent<Void?>()
    private val onFileDownloadRequest = SingleLiveEvent<Void?>()
    private val onShareToAnotherAppRequest = SingleLiveEvent<String>()
    private val onFileDownloadedForSharing = SingleLiveEvent<String>()
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)
    private var eventsRepository: IEventsRepository = EventsRepositoryImpl(application)

    fun initWith(
        record: Record,
        workspace: Workspace,
        isShownInSharedWithMe: Boolean,
        isShownInRootFolder: Boolean,
        currentArchiveNr: String? = null
    ) {
        this.record = record
        this.workspace = workspace
        this.isFragmentShownInSharedWithMe.value = isShownInSharedWithMe
        this.isFragmentShownInRootFolder.value = isShownInRootFolder
        publicLink =
            if (workspace == Workspace.PUBLIC_ARCHIVES) buildPublicLink(currentArchiveNr) else null
        actualAccessRole =
            record.accessRole?.getInferior(CurrentArchivePermissionsManager.instance.getAccessRole())
                ?: AccessRole.VIEWER

        _recordName.value = record.displayName ?: ""
        _recordSize.value = if (record.size != -1L) bytesToHumanReadableString(record.size) else ""
        _recordDate.value = record.displayDate.toDisplayDate()
        _recordThumb.value = if (record.type == RecordType.FILE) record.thumbnail256 ?: record.thumbURL200 ?: "" else ""
        _archiveThumb.value =
            record.archiveThumbURL200?.takeIf { isShownInSharedWithMe && isShownInRootFolder } ?: ""
        _archiveName.value =
            record.archiveFullName?.takeIf { isShownInSharedWithMe && isShownInRootFolder } ?: ""
        _accessRole.value = actualAccessRole

        _menuItems.value = buildMenuItems()
    }

    private fun buildMenuItems(): List<RecordMenuItem> {
        val baseItems = listOf(
            RecordMenuItem.Share,
            RecordMenuItem.Publish,
            RecordMenuItem.SendACopy,
            RecordMenuItem.GetLink,
            RecordMenuItem.Download,
            RecordMenuItem.Rename,
            RecordMenuItem.Move,
            RecordMenuItem.Copy,
            RecordMenuItem.Delete,
            RecordMenuItem.LeaveShare
        )

        // Mutable set of hidden options
        val hidden = mutableSetOf<RecordMenuItem>()

        // --- Record type specific rules ---
        if (record.type == RecordType.FOLDER) {
            hidden.add(RecordMenuItem.SendACopy)
            hidden.add(RecordMenuItem.Download)
        }

        // GetLink is the public web URL of the item, only built when browsing a public archive
        if (publicLink == null) hidden.add(RecordMenuItem.GetLink)

        // --- Workspace-specific rules ---
        when (workspace) {
            Workspace.PRIVATE_FILES -> {
                hidden.add(RecordMenuItem.LeaveShare)

                val perms = CurrentArchivePermissionsManager.instance
                if (!perms.isOwnershipAvailable()) hidden.add(RecordMenuItem.Share)
                if (!perms.isPublishAvailable()) hidden.add(RecordMenuItem.Publish)
                if (!perms.isEditAvailable()) hidden.add(RecordMenuItem.Rename)
                if (!perms.isMoveAvailable()) hidden.add(RecordMenuItem.Move)
                if (!perms.isCreateAvailable()) hidden.add(RecordMenuItem.Copy)
                if (!perms.isDeleteAvailable()) hidden.add(RecordMenuItem.Delete)
            }

            Workspace.FILE_VIEW_PRIVATE_FILES -> {
                hidden.addAll(
                    listOf(
                        RecordMenuItem.Rename,
                        RecordMenuItem.Move,
                        RecordMenuItem.Copy,
                        RecordMenuItem.Delete,
                        RecordMenuItem.LeaveShare
                    )
                )

                val perms = CurrentArchivePermissionsManager.instance
                if (!perms.isOwnershipAvailable()) hidden.add(RecordMenuItem.Share)
                if (!perms.isPublishAvailable()) hidden.add(RecordMenuItem.Publish)
            }

            Workspace.PUBLIC_FILES -> {
                hidden.addAll(
                    listOf(
                        RecordMenuItem.Share,
                        RecordMenuItem.Publish,
                        RecordMenuItem.LeaveShare
                    )
                )

                val perms = CurrentArchivePermissionsManager.instance
                if (!perms.isEditAvailable()) hidden.add(RecordMenuItem.Rename)
                if (!perms.isMoveAvailable()) hidden.add(RecordMenuItem.Move)
                if (!perms.isCreateAvailable()) hidden.add(RecordMenuItem.Copy)
                if (!perms.isDeleteAvailable()) hidden.add(RecordMenuItem.Delete)
            }

            Workspace.FILE_VIEW_PUBLIC_FILES -> {
                hidden.addAll(
                    listOf(
                        RecordMenuItem.Share,
                        RecordMenuItem.Publish,
                        RecordMenuItem.LeaveShare,
                        RecordMenuItem.Rename,
                        RecordMenuItem.Move,
                        RecordMenuItem.Copy,
                        RecordMenuItem.Delete
                    )
                )
            }

            Workspace.SHARES -> {
                hidden.addAll(
                    listOf(
                        RecordMenuItem.Publish,
                        RecordMenuItem.SendACopy
                    )
                )

                val isSharedWithMe = isFragmentShownInSharedWithMe.value == true
                val isRoot = isFragmentShownInRootFolder.value == true
                val role = actualAccessRole

                if (!role.isOwnershipAvailable() || isSharedWithMe)
                    hidden.add(RecordMenuItem.Share)

                if (!role.isEditAvailable())
                    hidden.add(RecordMenuItem.Rename)

                if (!role.isMoveAvailable() || isRoot)
                    hidden.add(RecordMenuItem.Move)

                if (!role.isCreateAvailable() || isRoot)
                    hidden.add(RecordMenuItem.Copy)

                if (!role.isDeleteAvailable() || (isSharedWithMe && isRoot))
                    hidden.add(RecordMenuItem.Delete)

                if (!isSharedWithMe || !isRoot)
                    hidden.add(RecordMenuItem.LeaveShare)
            }

            Workspace.FILE_VIEW_SHARED_FILES -> {
                hidden.addAll(
                    listOf(
                        RecordMenuItem.Publish,
                        RecordMenuItem.SendACopy,
                        RecordMenuItem.Rename,
                        RecordMenuItem.Move,
                        RecordMenuItem.Copy,
                        RecordMenuItem.Delete,
                        RecordMenuItem.LeaveShare,
                    )
                )

                val isSharedWithMe = isFragmentShownInSharedWithMe.value == true
                val role = actualAccessRole

                if (!role.isOwnershipAvailable() || isSharedWithMe)
                    hidden.add(RecordMenuItem.Share)
            }

            else -> { // Public Archive
                hidden.addAll(
                    listOf(
                        RecordMenuItem.Share,
                        RecordMenuItem.Publish,
                        RecordMenuItem.Download,
                        RecordMenuItem.Rename,
                        RecordMenuItem.Move,
                        RecordMenuItem.Copy,
                        RecordMenuItem.Delete,
                        RecordMenuItem.LeaveShare
                    )
                )
            }
        }

        // --- Final visible menu items ---
        return baseItems.filterNot { it in hidden }
    }

    private fun buildPublicLink(currentArchiveNr: String?): String? {
        val archiveNr = currentArchiveNr ?: return null
        return if (record.type == RecordType.FILE) {
            val parentFolderArchiveNr = record.parentFolderArchiveNr ?: return null
            val parentFolderLinkId = record.parentFolderLinkId ?: return null
            val recordArchiveNr = record.archiveNr ?: return null
            BuildConfig.BASE_URL + "p/archive/" + archiveNr + "/" + parentFolderArchiveNr +
                    "/" + parentFolderLinkId + "/record/" + recordArchiveNr
        } else {
            val folderArchiveNr = record.archiveNr ?: return null
            val folderLinkId = record.folderLinkId ?: return null
            BuildConfig.BASE_URL + "p/archive/" + archiveNr + "/" + folderArchiveNr +
                    "/" + folderLinkId
        }
    }

    fun copyPublicLinkToClipboard(): Boolean {
        val link = publicLink ?: return false
        val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(ctx.getString(R.string.share_management_share_link), link)
        clipboard.setPrimaryClip(clip)
        return true
    }

    fun onSendACopyClick() {
        requestFileData()
    }

    fun onDownloadClick() {
        checkForPermission()
    }

    private fun requestFileData() {
        val folderLinkId = record.folderLinkId
        val recordId = record.recordId

        if (folderLinkId != null && recordId != null) {
            _isBusyState.value = true
            fileRepository.getFileData(
                recordId, folderLinkId, record.archiveId,
                FileSessionData.allowsForeignStelaDetail, object : IFileDataListener {

                override fun onSuccess(newFileData: FileData) {
                    _isBusyState.value = false
                    fileData = newFileData
                    onShareToAnotherAppRequest.value = newFileData.contentType
                }

                override fun onFailed(error: String?) {
                    _isBusyState.value = false
                    showSnackbar.value = error
                }
            })
        }
    }

    private fun checkForPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
            && !DevicePermissionsHelper().hasWriteStoragePermission(ctx)
        ) {
            onRequestWritePermission.call()
        } else {
            onFileDownloadRequest.call()
        }
    }

    fun onWritePermissionGranted() {
        onFileDownloadRequest.call()
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
            ctx.contentResolver.query(
                collection, projection, null, null, null, null
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndex(idColumn)
                val nameIndex = cursor.getColumnIndex(nameColumn)

                while (cursor.moveToNext()) {
                    val fileName = cursor.getString(nameIndex)
                    if (fileName == fileData?.fileName) {
                        val fileId = cursor.getString(idIndex)
                        return "$collection/$fileId".toUri()
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
                    ctx,
                    PermanentApplication.instance.packageName + Constants.FILE_PROVIDER_NAME,
                    file
                )
            } else null
        }
        return null
    }

    fun downloadFileForSharing(lifecycleOwner: LifecycleOwner) {
        download = Download(ctx, record, this)
        download?.getWorkRequest()?.let { WorkManager.getInstance(ctx).enqueue(it) }
        download?.observeWorkInfoOn(lifecycleOwner)
        _isBusyState.value = true
    }

    fun sendEvent(action: EventAction, data: Map<String, String> = mapOf()) {
        eventsRepository.sendEventAction(
            eventAction = action,
            accountId = prefsHelper.getAccountId(),
            data = data
        )
    }

    override fun onFinished(download: Download, state: WorkInfo.State) {
        _isBusyState.value = false
        if (state == WorkInfo.State.SUCCEEDED) onFileDownloadedForSharing.value =
            fileData?.contentType
        else if (state == WorkInfo.State.FAILED)
            showSnackbar.value = ctx.getString(R.string.generic_error)
    }

    override fun onFinished(upload: Upload, succeeded: Boolean) {}

    override fun onFailedUpload(message: String) {}

    override fun onQuotaExceeded() {}

    fun getConfirmationMessageFor(item: RecordMenuItem): String {
        return when (item) {
            RecordMenuItem.Delete -> ctx.getString(R.string.confirm_delete_message, record.displayName)
            RecordMenuItem.LeaveShare -> ctx.getString(R.string.confirm_leave_share_message, record.displayName)
            else -> ""
        }
    }

    fun getConfirmationBoldTextFor(item: RecordMenuItem): String {
        return when (item) {
            RecordMenuItem.Delete,
            RecordMenuItem.LeaveShare -> record.displayName ?: ""
            else -> ""
        }
    }

    fun getConfirmationButtonLabelFor(item: RecordMenuItem): String {
        return when (item) {
            RecordMenuItem.Delete -> ctx.getString(R.string.delete)
            RecordMenuItem.LeaveShare -> ctx.getString(R.string.leave_share)
            else -> ctx.getString(R.string.confirm)
        }
    }

    fun getShowSnackbar(): LiveData<String> = showSnackbar
    fun getOnShareToAnotherAppRequest(): MutableLiveData<String> = onShareToAnotherAppRequest
    fun getOnFileDownloadedForSharing(): LiveData<String> = onFileDownloadedForSharing
    fun getOnRequestWritePermission(): MutableLiveData<Void?> = onRequestWritePermission
    fun getOnFileDownloadRequest(): MutableLiveData<Void?> = onFileDownloadRequest

    fun isTablet() = isTablet
}

enum class RecordMenuItem {
    Share,
    Publish,
    SendACopy,
    GetLink,
    Download,
    Rename,
    EditMetadata,
    Move,
    Copy,
    Delete,
    LeaveShare
}
