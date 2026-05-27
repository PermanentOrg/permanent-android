package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.EventAction
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.models.Share
import org.permanent.permanent.models.Status
import org.permanent.permanent.network.ILinkListener
import org.permanent.permanent.network.IRecordListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.ShareRequestType
import org.permanent.permanent.network.models.BackendRecordType
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.network.models.ShareLinkVO
import org.permanent.permanent.network.models.Shareby_urlVO
import org.permanent.permanent.repositories.EventsRepositoryImpl
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IEventsRepository
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.repositories.IShareRepository
import org.permanent.permanent.repositories.ShareRepositoryImpl
import org.permanent.permanent.repositories.StelaAccountRepository
import org.permanent.permanent.repositories.StelaAccountRepositoryImpl
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.bytesToHumanReadableString
import org.permanent.permanent.ui.pendingInvitationCount
import org.permanent.permanent.ui.composeComponents.TemporarySnackbarType
import org.permanent.permanent.ui.shareManagement.compose.AccessType
import org.permanent.permanent.ui.shareManagement.compose.LinkDuration
import org.permanent.permanent.ui.shareManagement.compose.SharePage
import org.permanent.permanent.ui.toBackendDateTimeString
import org.permanent.permanent.ui.toDisplayDate
import org.permanent.permanent.ui.toLocalDateUtc
import java.time.LocalDate


class ShareManagementViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private lateinit var record: Record
    private val _recordThumb = MutableStateFlow("")
    val recordThumb: StateFlow<String> = _recordThumb
    private val _recordSize = MutableStateFlow("")
    val recordSize: StateFlow<String> = _recordSize
    private val _recordDate = MutableStateFlow("")
    val recordDate: StateFlow<String> = _recordDate
    private val _recordName = MutableStateFlow("")
    val recordName: StateFlow<String> = _recordName
    private val _shareLink = MutableStateFlow("")
    val shareLink: StateFlow<String> = _shareLink
    private val _isCreatingLinkState = MutableStateFlow(false)
    val isCreatingLinkState: StateFlow<Boolean> = _isCreatingLinkState
    private val _isLinkSharedState = MutableStateFlow(false)
    val isLinkSharedState: StateFlow<Boolean> = _isLinkSharedState
    private val _createdAtDate = MutableStateFlow<LocalDate>(LocalDate.now())
    val createdAtDate: StateFlow<LocalDate> = _createdAtDate
    private val _selectedGeneralAccessType = MutableStateFlow(AccessType.ANYONE_CAN_VIEW)
    val selectedGeneralAccessType: StateFlow<AccessType> = _selectedGeneralAccessType
    private val _selectedAccessRole = MutableStateFlow(AccessRole.VIEWER)
    val selectedAccessRole: StateFlow<AccessRole> = _selectedAccessRole
    private val _selectedLinkDuration = MutableStateFlow(LinkDuration.NEVER)
    val selectedLinkDuration: StateFlow<LinkDuration> = _selectedLinkDuration
    private val _isBusyState = MutableStateFlow(false)
    val isBusyState: StateFlow<Boolean> = _isBusyState
    private val _snackbarMessage = MutableStateFlow("")
    val snackbarMessage: StateFlow<String> = _snackbarMessage
    private val _snackbarType = MutableStateFlow(TemporarySnackbarType.NONE)
    val snackbarType: StateFlow<TemporarySnackbarType> = _snackbarType
    private var shareLinkVO: ShareLinkVO? = null
    private val _editingShare = MutableStateFlow<Share?>(null)
    val editingShare: StateFlow<Share?> = _editingShare

    private val _editingArchiveAccessRole = MutableStateFlow(AccessRole.VIEWER)
    val editingArchiveAccessRole: StateFlow<AccessRole> = _editingArchiveAccessRole
    private val _accessRolesOpenedFrom = MutableStateFlow<SharePage?>(null)
    private val _navigateToPage = MutableStateFlow<SharePage?>(null)
    val navigateToPage: StateFlow<SharePage?> = _navigateToPage
    private val _pendingShares = MutableStateFlow<List<Share>>(emptyList())
    val pendingShares: StateFlow<List<Share>> = _pendingShares
    private val _approvedShares = MutableStateFlow<List<Share>>(emptyList())
    val approvedShares: StateFlow<List<Share>> = _approvedShares
    private val _isApprovingAll = MutableStateFlow(false)
    val isApprovingAll: StateFlow<Boolean> = _isApprovingAll
    private val _approvingShareIds = MutableStateFlow<Set<Int>>(emptySet())
    val approvingShareIds: StateFlow<Set<Int>> = _approvingShareIds
    private val _isRefreshingShares = MutableStateFlow(false)
    val isRefreshingShares: StateFlow<Boolean> = _isRefreshingShares
    private val _sharesChangedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sharesChangedEvent: SharedFlow<Unit> = _sharesChangedEvent.asSharedFlow()
    private val areLinkSettingsVisible = MutableLiveData(false)
    private var shareRepository: IShareRepository = ShareRepositoryImpl(appContext)
    private var eventsRepository: IEventsRepository = EventsRepositoryImpl(application)
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)
    private var stelaAccountRepository: StelaAccountRepository =
        StelaAccountRepositoryImpl(application)

    val activeAccessRole: StateFlow<AccessRole> =
        combine(
            selectedAccessRole,
            editingArchiveAccessRole,
            _accessRolesOpenedFrom
        ) { linkRole, archiveRole, openedFrom ->
            when (openedFrom) {
                SharePage.ARCHIVE_ACCESS -> archiveRole
                else -> linkRole
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = selectedAccessRole.value
        )

    fun setRecord(record: Record) {
        this.record = record

        _recordName.value = record.displayName ?: ""
        _recordSize.value = if (record.size != -1L) bytesToHumanReadableString(record.size) else ""
        _recordDate.value = record.displayDate.toDisplayDate()
        _recordThumb.value = if (record.type == RecordType.FILE) record.thumbnail256 ?: record.thumbURL200 ?: "" else ""

        checkForExistingLink(record)
        refreshShares()
    }

    private fun refreshShares() {
        val folderLinkId = record.folderLinkId ?: return
        _isRefreshingShares.value = true
        if (record.type == RecordType.FOLDER) {
            fileRepository.getFolder(folderLinkId, object : IRecordListener {
                override fun onSuccess(record: Record) = onRefreshSucceeded(record)
                override fun onFailed(error: String?) = onRefreshFailed(error)
            })
        } else {
            fileRepository.getRecord(folderLinkId, record.recordId)
                .enqueue(object : Callback<ResponseVO> {
                    override fun onResponse(
                        call: Call<ResponseVO>, response: Response<ResponseVO>
                    ) {
                        val freshRecord = response.body()?.getRecord()
                        if (freshRecord != null) onRefreshSucceeded(freshRecord)
                        else _isRefreshingShares.value = false
                    }

                    override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                        onRefreshFailed(t.message)
                    }
                })
        }
    }

    private fun onRefreshSucceeded(freshRecord: Record) {
        val previousPendingCount = record.pendingInvitationCount
        val newPendingCount = freshRecord.pendingInvitationCount
        record.shares = freshRecord.shares
        initShares(freshRecord.shares)
        _isRefreshingShares.value = false
        if (newPendingCount != previousPendingCount) {
            notifySharesChanged()
        }
    }

    private fun onRefreshFailed(error: String?) {
        _isRefreshingShares.value = false
        error?.let {
            _snackbarMessage.value = it
            _snackbarType.value = TemporarySnackbarType.ERROR
        }
    }

    private fun initShares(shares: MutableList<Share>?) {
        val pending = shares
            ?.filter { it.status.value == Status.PENDING }
            ?: emptyList()

        val approved = shares
            ?.filter { it.status.value != Status.PENDING }
            ?: emptyList()

        _pendingShares.value = pending
        _approvedShares.value = approved
    }

    private fun checkForExistingLink(record: Record) {
        if (_isBusyState.value) {
            return
        }

        _isBusyState.value = true
        shareRepository.requestShareLink(
            record,
            ShareRequestType.GET,
            object : IShareRepository.IShareByUrlListener {
                override fun onSuccess(shareByUrlVO: Shareby_urlVO?) {
                    _isBusyState.value = false
                    shareByUrlVO?.let { getLinkFromStela(it) }
                }

                override fun onFailed(error: String?) {
                    _isBusyState.value = false
                    error?.let {
                        _snackbarMessage.value = it
                        _snackbarType.value = TemporarySnackbarType.ERROR
                    }
                }
            })
    }

    private fun getLinkFromStela(shareByUrlVO: Shareby_urlVO) {
        if (_isBusyState.value) {
            return
        }

        shareByUrlVO.shareby_urlId?.let { shareByUrlId ->
            _isBusyState.value = true
            stelaAccountRepository.getShareLink(
                shareLinkIds = mutableListOf(shareByUrlId),
                listener = object : ILinkListener {

                    override fun onSuccess(shareLink: ShareLinkVO?) {
                        _isBusyState.value = false
                        shareLink?.let {
                            initLink(it)
                            initLinkSettings(it)
                        }
                    }

                    override fun onFailed(error: String?) {
                        _isBusyState.value = false
                        error?.let {
                            _snackbarMessage.value = it
                            _snackbarType.value = TemporarySnackbarType.ERROR
                        }
                    }
                })
        }
    }

    private fun initLink(shareLink: ShareLinkVO) {
        this.shareLinkVO = shareLink
        _shareLink.value = Constants.SHARED_LINK_URL + shareLink.token
        _isLinkSharedState.value = _shareLink.value != ""
        shareLinkVO?.createdAt?.toLocalDateUtc()?.let { _createdAtDate.value = it }
    }

    private fun initLinkSettings(shareLink: ShareLinkVO) {
        shareLink.accessRestrictions?.let { accessRestriction ->
            _selectedGeneralAccessType.value = AccessType.fromBackendValue(accessRestriction)
        }
        _selectedAccessRole.value =
            if (shareLink.permissionsLevel == AccessRole.MANAGER.lowerCase()) AccessRole.CURATOR else AccessRole.fromStelaBackendValue(
                shareLink.permissionsLevel
            )
        _selectedLinkDuration.value =
            LinkDuration.fromBackend(shareLink.createdAt, shareLink.expirationTimestamp)
    }

    fun onCreateLinkBtnClick() {
        if (_isCreatingLinkState.value) {
            return
        }

        _isCreatingLinkState.value = true
        val shareLinkVO = ShareLinkVO()
        shareLinkVO.itemId = record.id.toString()
        shareLinkVO.itemType =
            if (record.type == RecordType.FILE) BackendRecordType.RECORD.toString()
                .lowercase() else BackendRecordType.FOLDER.toString().lowercase()

        stelaAccountRepository.generateShareLink(shareLinkVO, object : ILinkListener {

            override fun onSuccess(shareLink: ShareLinkVO?) {
                _isCreatingLinkState.value = false
                _isLinkSharedState.value = true
                this@ShareManagementViewModel.shareLinkVO = shareLink
                _shareLink.value = Constants.SHARED_LINK_URL + shareLink?.token
                _isLinkSharedState.value = _shareLink.value != ""
                shareLink?.createdAt?.toLocalDateUtc()?.let { _createdAtDate.value = it }
                onShowLinkSettingsBtnClick()
            }

            override fun onFailed(error: String?) {
                _isCreatingLinkState.value = false
                _isLinkSharedState.value = false
                error?.let {
                    _snackbarMessage.value = it
                    _snackbarType.value = TemporarySnackbarType.ERROR
                }
            }
        })
    }

    fun copyLinkToClipboard() {
        val clipboard = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText(
            appContext.getString(R.string.share_management_share_link), _shareLink.value
        )
        clipboard.setPrimaryClip(clip)
    }

    fun cleanUrlRegex(url: String): String {
        return url.replace(Regex("^https?://(www\\.)?"), "")
    }

    fun onLinkSettingsBtnClick() {
        _navigateToPage.value = SharePage.LINK_SETTINGS
    }

    fun onShowLinkSettingsBtnClick() {
        areLinkSettingsVisible.value = true
    }

    fun onBackBtnClick(fromPage: SharePage) {
        when (fromPage) {
            SharePage.LINK_SETTINGS, SharePage.ARCHIVE_ACCESS -> _navigateToPage.value =
                SharePage.SHARE_ITEM

            SharePage.GENERAL_ACCESS -> _navigateToPage.value = SharePage.LINK_SETTINGS

            SharePage.ACCESS_ROLES -> {
                _navigateToPage.value = _accessRolesOpenedFrom.value
                _accessRolesOpenedFrom.value = null
            }

            else -> {
                Log.d("ShareManagementViewModel", "onBackBtnClick: ${_navigateToPage.value}")
            }
        }
    }

    fun onGeneralAccessClick() {
        _navigateToPage.value = SharePage.GENERAL_ACCESS
    }

    fun onGeneralAccessItemClick(type: AccessType) {
        _selectedGeneralAccessType.value = type
        _navigateToPage.value = SharePage.LINK_SETTINGS
    }

    fun onDefaultAccessRoleClick() {
        _accessRolesOpenedFrom.value = SharePage.LINK_SETTINGS
        _navigateToPage.value = SharePage.ACCESS_ROLES
    }

    fun onArchiveAccessRoleClick() {
        _accessRolesOpenedFrom.value = SharePage.ARCHIVE_ACCESS
        _navigateToPage.value = SharePage.ACCESS_ROLES
    }

    fun onAccessRoleClick(role: AccessRole) {
        if (_accessRolesOpenedFrom.value == SharePage.ARCHIVE_ACCESS) {
            _editingArchiveAccessRole.value = role
        } else {
            _selectedAccessRole.value = role
        }
        _navigateToPage.value = _accessRolesOpenedFrom.value ?: SharePage.SHARE_ITEM
        _accessRolesOpenedFrom.value = null
    }

    fun onLinkDurationSelected(duration: LinkDuration) {
        _selectedLinkDuration.value = duration
    }

    fun revokeLink() {
        if (_isBusyState.value) {
            return
        }
        _isBusyState.value = true
        shareLinkVO?.id?.let { shareId ->
            stelaAccountRepository.deleteShareLink(shareId, object : IResponseListener {

                override fun onSuccess(message: String?) {
                    _isBusyState.value = false
                    _isLinkSharedState.value = false
                    _navigateToPage.value = SharePage.SHARE_ITEM
                    _snackbarMessage.value = appContext.getString(R.string.link_revoked)
                    _snackbarType.value = TemporarySnackbarType.SUCCESS
                }

                override fun onFailed(error: String?) {
                    _isBusyState.value = false
                    error?.let {
                        _snackbarMessage.value = it
                        _snackbarType.value = TemporarySnackbarType.ERROR
                    }
                }
            })
        }
    }

    fun revokeAccess(share: Share) {
        if (_isBusyState.value) {
            return
        }

        _isBusyState.value = true
        shareRepository.deleteShare(share, object : IResponseListener {
            override fun onSuccess(message: String?) {
                _isBusyState.value = false
                _approvedShares.value = _approvedShares.value.filterNot { it.id == share.id }
                record.shares?.removeAll { it.id == share.id }
                _navigateToPage.value = SharePage.SHARE_ITEM
                _snackbarMessage.value = appContext.getString(R.string.access_revoked)
                _snackbarType.value = TemporarySnackbarType.SUCCESS
            }

            override fun onFailed(error: String?) {
                _isBusyState.value = false
                error?.let {
                    _snackbarMessage.value = it
                    _snackbarType.value = TemporarySnackbarType.ERROR
                }
            }
        })
    }

    fun onDoneBtnClick() {
        if (_isBusyState.value) {
            return
        }
        shareLinkVO?.id?.let { shareId ->
            val accessRestrictions = _selectedGeneralAccessType.value.backendValue
            val permissionLevel =
                if (_selectedGeneralAccessType.value == AccessType.ANYONE_CAN_VIEW) AccessRole.VIEWER.name.lowercase() else
                    if (_selectedAccessRole.value == AccessRole.CURATOR) AccessRole.MANAGER.name.lowercase() else
                        _selectedAccessRole.value.name.lowercase()
            val expirationTimestamp =
                shareLinkVO?.createdAt?.toLocalDateUtc()
                    ?.let { _selectedLinkDuration.value.expirationDate(it) }
                    ?.toBackendDateTimeString()

            _isBusyState.value = true
            val shareLinkVO = ShareLinkVO(
                id = shareId,
                permissionsLevel = permissionLevel,
                accessRestrictions = accessRestrictions,
                expirationTimestamp = expirationTimestamp
            )
            stelaAccountRepository.updateShareLink(shareLinkVO, object : IResponseListener {

                override fun onSuccess(message: String?) {
                    _isBusyState.value = false
                    _navigateToPage.value = SharePage.SHARE_ITEM
                    _snackbarMessage.value = appContext.getString(R.string.link_settings_updated)
                    _snackbarType.value = TemporarySnackbarType.SUCCESS
                }

                override fun onFailed(error: String?) {
                    _isBusyState.value = false
                    error?.let {
                        _snackbarMessage.value = it
                        _snackbarType.value = TemporarySnackbarType.ERROR
                    }
                }
            })
        }
    }

    fun onSaveBtnClick() {
        if (_isBusyState.value) {
            return
        }

        _isBusyState.value = true
        _editingShare.value?.let {
            it.accessRole = _editingArchiveAccessRole.value
            shareRepository.updateShare(it, object : IResponseListener {
                override fun onSuccess(message: String?) {
                    _isBusyState.value = false
                    _snackbarMessage.value = appContext.getString(R.string.archive_role_updated)
                    _snackbarType.value = TemporarySnackbarType.SUCCESS
                    _navigateToPage.value = SharePage.SHARE_ITEM
                }

                override fun onFailed(error: String?) {
                    _isBusyState.value = false
                    error?.let { errorMsg ->
                        _snackbarMessage.value = errorMsg
                        _snackbarType.value = TemporarySnackbarType.ERROR
                    }
                }
            })
        }
    }

    fun onEditClick(share: Share) {
        _editingShare.value = share
        _editingArchiveAccessRole.value = share.accessRole ?: AccessRole.VIEWER

        _accessRolesOpenedFrom.value = SharePage.ARCHIVE_ACCESS
        _navigateToPage.value = SharePage.ARCHIVE_ACCESS
    }

    private suspend fun approveShare(share: Share): Result<String?> =
        suspendCancellableCoroutine { cont ->
            shareRepository.updateShare(share, object : IResponseListener {
                override fun onSuccess(message: String?) {
                    cont.resume(Result.success(message))
                }

                override fun onFailed(error: String?) {
                    cont.resume(Result.failure(Exception(error)))
                }
            })
        }

    private fun applyApprovalSuccess(share: Share, notify: Boolean = true) {
        share.status.value = Status.OK
        _pendingShares.value = _pendingShares.value.filterNot { it.id == share.id }
        _approvedShares.value = _approvedShares.value + share
        if (notify) notifySharesChanged()
    }

    private fun notifySharesChanged() {
        _sharesChangedEvent.tryEmit(Unit)
    }

    fun onApproveClick(share: Share) {
        if (_isBusyState.value) {
            return
        }

        _isBusyState.value = true
        viewModelScope.launch {
            approveShare(share).onSuccess { message ->
                    _isBusyState.value = false
                    applyApprovalSuccess(share)
                    message?.let {
                        _snackbarMessage.value = it
                        _snackbarType.value = TemporarySnackbarType.SUCCESS
                    }
                }
                .onFailure { e ->
                    _isBusyState.value = false
                    val errorMsg = e.message
                    if (!errorMsg.isNullOrEmpty()) {
                        _snackbarMessage.value = errorMsg
                        _snackbarType.value = TemporarySnackbarType.ERROR
                    }
                }
        }
    }

    fun approveAllPendingShares() {
        if (_isBusyState.value) {
            return
        }
        val toProcess = _pendingShares.value
        if (toProcess.isEmpty()) {
            return
        }

        _isApprovingAll.value = true
        viewModelScope.launch {
            var successCount = 0
            var failureCount = 0
            for (share in toProcess) {
                val id = share.id ?: continue
                _approvingShareIds.value = _approvingShareIds.value + id
                approveShare(share)
                    .onSuccess {
                        applyApprovalSuccess(share, notify = false)
                        successCount++
                    }
                    .onFailure { failureCount++ }
                _approvingShareIds.value = _approvingShareIds.value - id
            }
            if (successCount > 0) notifySharesChanged()
            val (messageRes, type) = when {
                failureCount == 0 ->
                    R.string.approve_all_success to TemporarySnackbarType.SUCCESS
                successCount == 0 ->
                    R.string.approve_all_failure to TemporarySnackbarType.ERROR
                else ->
                    R.string.approve_all_partial_failure to TemporarySnackbarType.WARNING
            }
            _snackbarMessage.value = appContext.getString(messageRes)
            _snackbarType.value = type
            _isApprovingAll.value = false
        }
    }

    fun onDenyClick(share: Share) {
        if (_isBusyState.value) {
            return
        }

        _isBusyState.value = true
        shareRepository.deleteShare(share, object : IResponseListener {
            override fun onSuccess(message: String?) {
                _isBusyState.value = false
                _pendingShares.value = _pendingShares.value.filterNot { it.id == share.id }
                record.shares?.removeAll { it.id == share.id }
                notifySharesChanged()
                message?.let {
                    _snackbarMessage.value = it
                    _snackbarType.value = TemporarySnackbarType.SUCCESS
                }
            }

            override fun onFailed(error: String?) {
                _isBusyState.value = false
                error?.let { errorMsg ->
                    _snackbarMessage.value = errorMsg
                    _snackbarType.value = TemporarySnackbarType.ERROR
                }
            }
        })
    }

    fun sendEvent(action: EventAction, data: Map<String, String> = mapOf()) {
        eventsRepository.sendEventAction(
            eventAction = action,
            accountId = prefsHelper.getAccountId(),
            data = data
        )
    }

    fun clearEditingShare() {
        _editingShare.value = null
    }

    fun clearPageNavigation() {
        _navigateToPage.value = null
    }

    fun clearSnackbar() {
        _snackbarMessage.value = ""
    }

    fun getRecord(): Record = record
}
