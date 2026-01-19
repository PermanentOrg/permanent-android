package org.permanent.permanent.viewmodels

import android.app.Application
import android.app.DatePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.Editable
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.DatePicker
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.EventAction
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.models.Share
import org.permanent.permanent.models.Status
import org.permanent.permanent.network.ILinkListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.ShareRequestType
import org.permanent.permanent.network.models.BackendRecordType
import org.permanent.permanent.network.models.ShareLinkVO
import org.permanent.permanent.network.models.Shareby_urlVO
import org.permanent.permanent.repositories.EventsRepositoryImpl
import org.permanent.permanent.repositories.IEventsRepository
import org.permanent.permanent.repositories.IShareRepository
import org.permanent.permanent.repositories.ShareRepositoryImpl
import org.permanent.permanent.repositories.StelaAccountRepository
import org.permanent.permanent.repositories.StelaAccountRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.bytesToHumanReadableString
import org.permanent.permanent.ui.composeComponents.SnackbarType
import org.permanent.permanent.ui.shareManagement.ShareListener
import org.permanent.permanent.ui.shareManagement.compose.AccessType
import org.permanent.permanent.ui.shareManagement.compose.LinkDuration
import org.permanent.permanent.ui.shareManagement.compose.SharePage
import org.permanent.permanent.ui.toBackendDateTimeString
import org.permanent.permanent.ui.toDisplayDate
import org.permanent.permanent.ui.toLocalDateUtc
import java.time.LocalDate


class ShareManagementViewModel(application: Application) : ObservableAndroidViewModel(application),
    ShareListener {

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
    private val _snackbarType = MutableStateFlow(SnackbarType.NONE)
    val snackbarType: StateFlow<SnackbarType> = _snackbarType
    private var shareLinkVO: ShareLinkVO? = null
    private val _navigateToPage = MutableStateFlow<SharePage?>(null)
    val navigateToPage: StateFlow<SharePage?> = _navigateToPage
    private var shares = mutableListOf<Share>()
    private var pendingShares = mutableListOf<Share>()
    private val sharesSize = MutableLiveData(0)
    private val pendingSharesSize = MutableLiveData(0)
    private val sharePreview = MutableLiveData(false)
    private val autoApprove = MutableLiveData(false)
    private val maxUses = MutableLiveData("0")
    private val defaultAccessRole = MutableLiveData(AccessRole.VIEWER)
    private val expirationDate = MutableLiveData<String>()
    private val showDatePicker = SingleLiveEvent<Void?>()
    private val areLinkSettingsVisible = MutableLiveData(false)
    private val showSnackbar = MutableLiveData<String>()
    private val showSnackbarSuccess = MutableLiveData<String>()
    private val onShareLinkRequest = SingleLiveEvent<String>()
    private val showAccessRolesForShare = SingleLiveEvent<Share>()
    private val onShareApproved = SingleLiveEvent<Share>()
    private val onShareDenied = SingleLiveEvent<Share>()
    private var shareRepository: IShareRepository = ShareRepositoryImpl(appContext)
    private var eventsRepository: IEventsRepository = EventsRepositoryImpl(application)
    private var stelaAccountRepository: StelaAccountRepository =
        StelaAccountRepositoryImpl(application)

    fun setRecord(record: Record) {
        this.record = record

        _recordName.value = record.displayName ?: ""
        _recordSize.value = if (record.size != -1L) bytesToHumanReadableString(record.size) else ""
        _recordDate.value = record.displayDate.toDisplayDate()
        _recordThumb.value = if (record.type == RecordType.FILE) record.thumbURL200 ?: "" else ""

        checkForExistingLink(record)
        initShares(record.shares)
    }

    private fun initShares(shares: MutableList<Share>?) {
        this.shares =
            shares?.filter { it.status.value != Status.PENDING }?.toMutableList() ?: mutableListOf()
        this.pendingShares =
            shares?.filter { it.status.value == Status.PENDING }?.toMutableList() ?: mutableListOf()
        sharesSize.value = this.shares.size
        pendingSharesSize.value = this.pendingShares.size
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
                    error?.let { showSnackbar.value = error }
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
                mutableListOf(shareByUrlId),
                object : ILinkListener {

                    override fun onSuccess(shareLink: ShareLinkVO?) {
                        _isBusyState.value = false
                        shareLink?.let {
                            initLink(it)
                            initLinkSettings(it)
                        }
                    }

                    override fun onFailed(error: String?) {
                        _isBusyState.value = false
                        error?.let { showSnackbar.value = error }
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
            _selectedGeneralAccessType.value =
                AccessType.fromBackendValue(accessRestriction) ?: AccessType.ANYONE_CAN_VIEW
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
                    showSnackbar.value = error
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

    fun onShareLinkBtnClick() {
        onShareLinkRequest.value = shareLink.value.toString()
    }

    fun onLinkSettingsBtnClick() {
        _navigateToPage.value = SharePage.LINK_SETTINGS
    }

    fun onShowLinkSettingsBtnClick() {
        areLinkSettingsVisible.value = true
    }

    fun onBackBtnClick(fromPage: SharePage) {
        when (fromPage) {
            SharePage.LINK_SETTINGS -> {
                _navigateToPage.value = SharePage.SHARE_ITEM
            }

            SharePage.GENERAL_ACCESS, SharePage.ACCESS_ROLES -> {
                _navigateToPage.value = SharePage.LINK_SETTINGS
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
        _navigateToPage.value = SharePage.ACCESS_ROLES
    }

    fun onAccessRoleClick(role: AccessRole) {
        _selectedAccessRole.value = role
        _navigateToPage.value = SharePage.LINK_SETTINGS
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
                    _snackbarType.value = SnackbarType.SUCCESS
                }

                override fun onFailed(error: String?) {
                    _isBusyState.value = false
                    error?.let {
                        _snackbarMessage.value = it
                        _snackbarType.value = SnackbarType.ERROR
                    }
                }
            })
        }
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
                    _snackbarType.value = SnackbarType.SUCCESS
                }

                override fun onFailed(error: String?) {
                    _isBusyState.value = false
                    error?.let {
                        _snackbarMessage.value = it
                        _snackbarType.value = SnackbarType.ERROR
                    }
                }
            })
        }
    }

    override fun onEditClick(share: Share) {
        showAccessRolesForShare.value = share
    }

    override fun onApproveClick(share: Share) {
        if (_isCreatingLinkState.value != null && _isCreatingLinkState.value!!) {
            return
        }

        _isCreatingLinkState.value = true
        shareRepository.updateShare(share, object : IResponseListener {
            override fun onSuccess(message: String?) {
                _isCreatingLinkState.value = false
                share.status.value = Status.OK // This hides the Approve and Deny buttons
                pendingShares.remove(share)
                shares.add(share)
                pendingSharesSize.value = pendingShares.size
                sharesSize.value = shares.size
                onShareApproved.value = share
                showSnackbarSuccess.value = message
            }

            override fun onFailed(error: String?) {
                _isCreatingLinkState.value = false
                showSnackbar.value = error
            }
        })
    }

    override fun onDenyClick(share: Share) {
        if (_isCreatingLinkState.value != null && _isCreatingLinkState.value!!) {
            return
        }

        _isCreatingLinkState.value = true
        shareRepository.deleteShare(share, object : IResponseListener {
            override fun onSuccess(message: String?) {
                _isCreatingLinkState.value = false
                pendingShares.remove(share)
                pendingSharesSize.value = pendingShares.size
                showSnackbarSuccess.value = message
                onShareDenied.value = share // Removes share from adapter
            }

            override fun onFailed(error: String?) {
                _isCreatingLinkState.value = false
                showSnackbar.value = error
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

    fun onShareRemoved(share: Share) {
        shares.remove(share)
        sharesSize.value = shares.size
    }

    fun onAccessRoleUpdated(accessRole: AccessRole) {
        defaultAccessRole.value = accessRole
    }

    fun clearPageNavigation() {
        _navigateToPage.value = null
    }

    fun clearSnackbar() {
        _snackbarMessage.value = ""
    }

    fun getShares(): List<Share> = shares

    fun getPendingShares(): List<Share> = pendingShares

    fun getRecord(): Record = record

    fun getSharesSize(): MutableLiveData<Int> = sharesSize

    fun getPendingSharesSize(): MutableLiveData<Int> = pendingSharesSize

    fun getSharePreview(): MutableLiveData<Boolean> = sharePreview

    fun getAutoApprove(): MutableLiveData<Boolean> = autoApprove

    fun getMaxUses(): MutableLiveData<String> = maxUses

    fun getAccessRole(): MutableLiveData<AccessRole> = defaultAccessRole

    fun getExpirationDate(): MutableLiveData<String> = expirationDate

    fun getShowDatePicker(): LiveData<Void?> = showDatePicker

    fun getShowSnackbar(): LiveData<String> = showSnackbar

    fun getShowSnackbarSuccess(): LiveData<String> = showSnackbarSuccess

    fun getOnShareLinkRequest(): LiveData<String> = onShareLinkRequest

    fun getShowAccessRolesForShare(): LiveData<Share> = showAccessRolesForShare

    fun getOnShareApproved(): MutableLiveData<Share> = onShareApproved

    fun getOnShareDenied(): MutableLiveData<Share> = onShareDenied
}
