package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.permanent.permanent.R
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Archive
import org.permanent.permanent.models.ArchiveType
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.models.Status
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.ILinkListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.network.models.IFolderChildrenListener
import org.permanent.permanent.network.models.ShareLinkVO
import org.permanent.permanent.network.models.Shareby_urlVO
import org.permanent.permanent.repositories.ArchiveRepositoryImpl
import org.permanent.permanent.repositories.IArchiveRepository
import org.permanent.permanent.repositories.IShareRepository
import org.permanent.permanent.repositories.RequestShareAccessResult
import org.permanent.permanent.repositories.ShareRepositoryImpl
import org.permanent.permanent.repositories.StelaAccountRepository
import org.permanent.permanent.repositories.StelaAccountRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.shareManagement.compose.AccessType
import org.permanent.permanent.ui.shares.ShareActionUiState
import org.permanent.permanent.ui.shares.SharePreviewNavEvent

private const val ACCESS_RESTRICTION_NONE = "none"

class SharePreviewViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private lateinit var urlToken: String
    private var cachedShareByUrl: Shareby_urlVO? = null
    private var cachedAccessRestrictions: String? = null
    private var cachedPermissionsLevel: String? = null
    private var originalArchive: Archive? = null
    private var openWasTapped: Boolean = false

    private val _archiveThumbURL = MutableStateFlow("")
    private val recordDisplayName = SingleLiveEvent<String>()
    private val _rawAccountName = MutableStateFlow("")
    private val _rawArchiveName = MutableStateFlow("")
    private val _records = MutableStateFlow<List<Record>>(emptyList())
    private val _accessType = MutableStateFlow<AccessType?>(null)
    private val _actionUiState =
        MutableStateFlow<ShareActionUiState>(ShareActionUiState.SelectArchive)
    private val onSharePreviewNavEvent = SingleLiveEvent<SharePreviewNavEvent>()
    private val _isBusy = MutableStateFlow(false)
    private val errorMessage = MutableLiveData<String>()
    private val _archives = MutableStateFlow<List<Archive>>(emptyList())
    private val _selectedArchive = MutableStateFlow<Archive?>(null)
    private val _createArchiveCompletedTick = MutableStateFlow(0L)
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private var shareRepository: IShareRepository = ShareRepositoryImpl(application)
    private var archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)
    private var stelaAccountRepository: StelaAccountRepository =
        StelaAccountRepositoryImpl(application)

    fun checkShareLink(urlToken: String) {
        this.urlToken = urlToken
        if (_isBusy.value) return

        _isBusy.value = true
        loadShareData(isFirstLoad = true)
    }

    private fun loadShareData(isFirstLoad: Boolean) {
        shareRepository.checkShareLink(urlToken, object : IShareRepository.IShareByUrlListener {
            override fun onSuccess(shareByUrlVO: Shareby_urlVO?) {
                cachedShareByUrl = shareByUrlVO
                // Loading data in the header
                _archiveThumbURL.value =
                    shareByUrlVO?.ArchiveVO?.thumbnail256
                        ?: shareByUrlVO?.ArchiveVO?.thumbURL200
                        ?: ""
                _rawAccountName.value = shareByUrlVO?.AccountVO?.fullName ?: ""
                _rawArchiveName.value = "The ${shareByUrlVO?.ArchiveVO?.fullName} Archive"

                // Loading toolbar title
                when {
                    shareByUrlVO?.RecordVO != null ->
                        recordDisplayName.value = shareByUrlVO.RecordVO?.displayName ?: ""
                    shareByUrlVO?.FolderVO != null ->
                        recordDisplayName.value = shareByUrlVO.FolderVO?.displayName ?: ""
                }

                if (isFirstLoad) {
                    fetchShareLinkAndArchives(shareByUrlVO)
                } else {
                    val newState = deriveActionUiState(shareByUrlVO)
                    _actionUiState.value = newState
                    val userHasAccess = newState is ShareActionUiState.Approved ||
                        newState is ShareActionUiState.OwnedByMe
                    if (userHasAccess) {
                        when {
                            shareByUrlVO?.RecordVO != null -> {
                                _records.value = listOf(Record(shareByUrlVO))
                                _isBusy.value = false
                            }
                            shareByUrlVO?.FolderVO?.folderId != null -> {
                                loadRealFolderChildren(shareByUrlVO.FolderVO?.folderId!!)
                            }
                            else -> {
                                _isBusy.value = false
                            }
                        }
                    } else {
                        _records.value = getFakeBlurredRecords()
                        _isBusy.value = false
                    }
                }
            }

            override fun onFailed(error: String?) {
                _isBusy.value = false
                _actionUiState.value = ShareActionUiState.Error
                errorMessage.value = error
            }
        })
    }

    private fun fetchShareLinkAndArchives(shareByUrlVO: Shareby_urlVO?) {
        stelaAccountRepository.getShareLink(
            shareTokens = mutableListOf(urlToken),
            listener = object : ILinkListener {

                override fun onSuccess(shareLink: ShareLinkVO?) {
                    cachedAccessRestrictions = shareLink?.accessRestrictions
                    cachedPermissionsLevel = shareLink?.permissionsLevel

                    val access =
                        shareLink?.accessRestrictions?.let { AccessType.fromBackendValue(it) }
                    _accessType.value = access

                    val folderId = shareByUrlVO?.FolderVO?.folderId
                    when {
                        access != AccessType.ANYONE_CAN_VIEW -> {
                            _records.value = getFakeBlurredRecords()
                            _isBusy.value = false
                        }
                        shareByUrlVO?.RecordVO != null -> {
                            _records.value = listOf(Record(shareByUrlVO))
                            _isBusy.value = false
                        }
                        folderId != null -> loadRealFolderChildren(folderId)
                        else -> _isBusy.value = false
                    }
                }

                override fun onFailed(error: String?) {
                    _isBusy.value = false
                    _actionUiState.value = ShareActionUiState.Error
                    errorMessage.value = error
                }
            }
        )

        archiveRepository.getAllArchives(object : IDataListener {
            override fun onSuccess(dataList: List<Datum>?) {
                val notPending = dataList
                    ?.mapNotNull { it.ArchiveVO?.let(::Archive) }
                    ?.filter { it.status != Status.PENDING }
                    .orEmpty()
                _archives.value = notPending
                if (originalArchive == null) {
                    val currentId = prefsHelper.getCurrentArchiveId()
                    originalArchive = notPending.firstOrNull { it.id == currentId }
                }
            }

            override fun onFailed(error: String?) {
                // Silent — the picker sheet will simply show its empty state.
            }
        })
    }

    fun shouldRestoreArchiveOnBack(): Boolean {
        if (openWasTapped) return false
        val original = originalArchive ?: return false
        if (original.number.isNullOrEmpty()) return false
        return original.id != prefsHelper.getCurrentArchiveId()
    }

    fun restoreOriginalArchiveIfChanged(onComplete: () -> Unit) {
        if (!shouldRestoreArchiveOnBack()) {
            onComplete()
            return
        }
        val original = originalArchive!!

        _isBusy.value = true
        saveCurrentArchive(original)
        archiveRepository.switchToArchive(original.number!!, object : IDataListener {
            override fun onSuccess(dataList: List<Datum>?) {
                _isBusy.value = false
                onComplete()
            }
            override fun onFailed(error: String?) {
                _isBusy.value = false
                onComplete()
            }
        })
    }

    private fun saveCurrentArchive(archive: Archive) {
        prefsHelper.saveCurrentArchiveInfo(
            archive.id,
            archive.number,
            archive.type,
            archive.fullName,
            archive.thumbnail256 ?: archive.thumbURL200,
            archive.accessRole
        )
    }

    private fun loadRealFolderChildren(folderId: Int) {
        _isBusy.value = true

        stelaAccountRepository.getFolderChildren(
            shareToken = urlToken,
            folderId = folderId,
            listener = object : IFolderChildrenListener {

                override fun onSuccess(records: List<Record>) {
                    _isBusy.value = false
                    _records.value = mapToPreviewLayout(records)
                }

                override fun onFailed(error: String?) {
                    _isBusy.value = false
                    _actionUiState.value = ShareActionUiState.Error
                    errorMessage.value = error
                }
            }
        )
    }

    private fun mapToPreviewLayout(records: List<Record>): List<Record> {
        val folders = records.filter { it.type == RecordType.FOLDER }
        val images = records.filter { it.type == RecordType.FILE }

        if (images.isEmpty()) {
            return folders.take(4)
        }

        val result = MutableList<Record?>(4) { null }

        var usedFolders = 0
        if (folders.isNotEmpty()) {
            result[0] = folders[0]
            usedFolders = 1
        }

        when (images.size) {
            1 -> {
                result[3] = images[0]
            }

            2 -> {
                result[2] = images[0]
                result[3] = images[1]
            }

            else -> {
                val startIndex = if (folders.isNotEmpty()) 1 else 0
                val maxImages = 4 - startIndex

                images.take(maxImages).forEachIndexed { index, image ->
                    result[startIndex + index] = image
                }
            }
        }

        val remainingFolders = folders.drop(usedFolders).iterator()

        for (i in result.indices) {
            if (result[i] == null && remainingFolders.hasNext()) {
                result[i] = remainingFolders.next()
            }
        }

        return result.filterNotNull()
    }

    fun onArchiveSelected(archive: Archive) {
        if (_isBusy.value || archive.id == _selectedArchive.value?.id) return
        val previous = _selectedArchive.value
        _selectedArchive.value = archive
        val archiveNr = archive.number
        if (archiveNr.isNullOrEmpty()) {
            _selectedArchive.value = previous
            return
        }
        _isBusy.value = true
        _actionUiState.value = ShareActionUiState.Loading
        archiveRepository.switchToArchive(archiveNr, object : IDataListener {
            override fun onSuccess(dataList: List<Datum>?) {
                saveCurrentArchive(archive)
                loadShareData(isFirstLoad = false)
            }

            override fun onFailed(error: String?) {
                _isBusy.value = false
                _selectedArchive.value = previous
                cachedShareByUrl?.let { _actionUiState.value = deriveActionUiState(it) }
                errorMessage.value = error
            }
        })
    }

    fun onCreateArchive(name: String, type: ArchiveType) {
        if (_isBusy.value) return
        _isBusy.value = true
        _actionUiState.value = ShareActionUiState.Loading

        archiveRepository.createNewArchive(
            name,
            type,
            object : IArchiveRepository.IArchiveListener {
                override fun onSuccess(archive: Archive) {
                    _archives.value = _archives.value + archive
                    _isBusy.value = false
                    _createArchiveCompletedTick.value =
                        _createArchiveCompletedTick.value + 1
                    onArchiveSelected(archive)
                }

                override fun onFailed(error: String?) {
                    _isBusy.value = false
                    cachedShareByUrl?.let { _actionUiState.value = deriveActionUiState(it) }
                    errorMessage.value = error
                }
            }
        )
    }

    val createArchiveCompletedTick: StateFlow<Long> = _createArchiveCompletedTick.asStateFlow()

    fun onRequestAccessBtnClick() {
        if (_isBusy.value) return
        _isBusy.value = true
        _actionUiState.value = ShareActionUiState.Loading
        shareRepository.requestShareAccess(urlToken) { result ->
            when (result) {
                is RequestShareAccessResult.Success,
                RequestShareAccessResult.AlreadyExists -> {
                    loadShareData(isFirstLoad = false)
                }
                is RequestShareAccessResult.Error -> {
                    _isBusy.value = false
                    errorMessage.value = result.message
                    cachedShareByUrl?.let { _actionUiState.value = deriveActionUiState(it) }
                }
            }
        }
    }

    fun onOpenBtnClick() {
        val share = cachedShareByUrl ?: return
        if (isCreator(share)) {
            openWasTapped = true
            onSharePreviewNavEvent.value =
                SharePreviewNavEvent.OpenSharedByMe(share.FolderVO?.folderId)
            return
        }
        if (_isBusy.value) return
        _isBusy.value = true
        shareRepository.requestShareAccess(urlToken) { result ->
            when (result) {
                is RequestShareAccessResult.Success,
                RequestShareAccessResult.AlreadyExists -> {
                    _isBusy.value = false
                    openWasTapped = true
                    val itemId = share.RecordVO?.recordId ?: share.FolderVO?.folderId
                    onSharePreviewNavEvent.value = SharePreviewNavEvent.OpenSharedWithMe(itemId)
                }
                is RequestShareAccessResult.Error -> {
                    _isBusy.value = false
                    errorMessage.value = result.message
                }
            }
        }
    }

    private fun isCreator(share: Shareby_urlVO): Boolean =
        share.byAccountId == prefsHelper.getAccountId() &&
            share.byArchiveId == prefsHelper.getCurrentArchiveId()

    private fun deriveActionUiState(shareByUrl: Shareby_urlVO?): ShareActionUiState {
        if (shareByUrl == null) return ShareActionUiState.Loading
        if (isCreator(shareByUrl)) return ShareActionUiState.OwnedByMe

        val isUnrestricted = cachedAccessRestrictions == ACCESS_RESTRICTION_NONE
        val status = shareByUrl.ShareVO?.status?.lowercase().orEmpty()
        return when {
            "ok" in status || isUnrestricted -> ShareActionUiState.Approved(
                accessRole = shareByUrl.ShareVO?.accessRole
                    ?.let { AccessRole.fromBackendValue(it) }
                    ?: cachedPermissionsLevel?.let { AccessRole.fromStelaBackendValue(it) }
            )
            "pending" in status -> ShareActionUiState.AccessRequested
            else -> ShareActionUiState.RequestAccess
        }
    }

    private fun getFakeBlurredRecords(): List<Record> {
        return listOf(
            Record(recordId = -1, folderLinkId = -1).apply {
                type = RecordType.FOLDER
                displayName = "Folder"
                isThumbBlurred = true
            },

            Record(recordId = -2, folderLinkId = -1).apply {
                isThumbBlurred = true
                localDrawableRes = R.drawable.img_share_preview_tall
            },
            Record(recordId = -3, folderLinkId = -1).apply {
                isThumbBlurred = true
                localDrawableRes = R.drawable.img_share_preview_small
            },
            Record(recordId = -4, folderLinkId = -1).apply {
                isThumbBlurred = true
                localDrawableRes = R.drawable.img_share_preview_large
            }
        )
    }

    val archiveThumbURL: StateFlow<String> = _archiveThumbURL.asStateFlow()

    fun getRecordDisplayName(): MutableLiveData<String> = recordDisplayName

    fun getOnSharePreviewNavEvent(): MutableLiveData<SharePreviewNavEvent> = onSharePreviewNavEvent

    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    fun getErrorMessage(): MutableLiveData<String> = errorMessage

    val rawAccountName: StateFlow<String> = _rawAccountName.asStateFlow()

    val rawArchiveName: StateFlow<String> = _rawArchiveName.asStateFlow()

    val records: StateFlow<List<Record>> = _records.asStateFlow()

    val accessType: StateFlow<AccessType?> = _accessType.asStateFlow()

    val archives: StateFlow<List<Archive>> = _archives.asStateFlow()

    val selectedArchive: StateFlow<Archive?> = _selectedArchive.asStateFlow()

    val actionUiState: StateFlow<ShareActionUiState> = _actionUiState.asStateFlow()
}
