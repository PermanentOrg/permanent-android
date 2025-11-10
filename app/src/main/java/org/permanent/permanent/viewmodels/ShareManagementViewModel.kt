package org.permanent.permanent.viewmodels

import android.app.Application
import android.app.DatePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.Editable
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.DatePicker
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.internal.trimSubstring
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.R
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Archive
import org.permanent.permanent.models.EventAction
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.models.Share
import org.permanent.permanent.models.Status
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.ShareRequestType
import org.permanent.permanent.network.models.Shareby_urlVO
import org.permanent.permanent.repositories.EventsRepositoryImpl
import org.permanent.permanent.repositories.IEventsRepository
import org.permanent.permanent.repositories.IShareRepository
import org.permanent.permanent.repositories.ShareRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.bytesToHumanReadableString
import org.permanent.permanent.ui.shareManagement.ShareListener
import org.permanent.permanent.ui.toDisplayDate


class ShareManagementViewModel(application: Application) : ObservableAndroidViewModel(application),
    ShareListener, DatePickerDialog.OnDateSetListener {

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
    private var shareByUrlVO: Shareby_urlVO? = null


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
    private val sharedWithLabelTxt = MutableLiveData<String>()
    private val areLinkSettingsVisible = MutableLiveData(false)
    private val showSnackbar = MutableLiveData<String>()
    private val showSnackbarSuccess = MutableLiveData<String>()
    private val onShareLinkRequest = SingleLiveEvent<String>()
    private val onRevokeLinkRequest = SingleLiveEvent<Void?>()
    private val showAccessRolesForLink = SingleLiveEvent<Shareby_urlVO>()
    private val showAccessRolesForShare = SingleLiveEvent<Share>()
    private val onShareApproved = SingleLiveEvent<Share>()
    private val onShareDenied = SingleLiveEvent<Share>()
    private var shareRepository: IShareRepository = ShareRepositoryImpl(appContext)
    private var eventsRepository: IEventsRepository = EventsRepositoryImpl(application)

    fun setRecord(record: Record) {
        this.record = record

        _recordName.value = record.displayName ?: ""
        _recordSize.value = if (record.size != -1L) bytesToHumanReadableString(record.size) else ""
        _recordDate.value = record.displayDate.toDisplayDate()
        _recordThumb.value = if (record.type == RecordType.FILE) record.thumbURL200 ?: "" else ""

        initShares(record.shares)
        sharedWithLabelTxt.value =
            appContext.getString(R.string.record_options_shared_with, shares.size)
    }

    fun setShareLink(shareByUrlVO: Shareby_urlVO?) {
        if (shareByUrlVO == null) {
            checkForExistingLink(record)
        } else {
            init(shareByUrlVO)
        }
    }

    private fun initShares(shares: MutableList<Share>?) {
        this.shares =
            shares?.filter { it.status.value != Status.PENDING }?.toMutableList() ?: mutableListOf()
        this.pendingShares =
            shares?.filter { it.status.value == Status.PENDING }?.toMutableList() ?: mutableListOf()
        sharesSize.value = this.shares.size
        pendingSharesSize.value = this.pendingShares.size
    }

    private fun init(shareByUrlVO: Shareby_urlVO) {
        this.shareByUrlVO = shareByUrlVO
        _shareLink.value = shareByUrlVO.shareUrl ?: ""
        _isLinkSharedState.value = _shareLink.value != ""
        sharePreview.value = shareByUrlVO.previewToggle == 1
        autoApprove.value = shareByUrlVO.autoApproveToggle == 1
        maxUses.value = (shareByUrlVO.maxUses ?: 0).toString()
        defaultAccessRole.value = AccessRole.createFromBackendString(shareByUrlVO.defaultAccessRole)
        shareByUrlVO.expiresDT.let { expirationDate.value = it?.trimSubstring() }
    }

    private fun checkForExistingLink(record: Record) {
        if (isCreatingLinkState.value!!) {
            return
        }

        _isCreatingLinkState.value = true
        shareRepository.requestShareLink(record, ShareRequestType.GET,
            object : IShareRepository.IShareByUrlListener {
                override fun onSuccess(shareByUrlVO: Shareby_urlVO?) {
                    _isCreatingLinkState.value = false
                    _isLinkSharedState.value = true
                    shareByUrlVO?.let { init(it) }
                }

                override fun onFailed(error: String?) {
                    _isCreatingLinkState.value = false
                    showSnackbar.value = error
                }
            })
    }

    fun onCreateLinkBtnClick() {
        if (_isCreatingLinkState.value!!) {
            return
        }

        _isCreatingLinkState.value = true
        shareRepository.requestShareLink(record, ShareRequestType.GENERATE,
            object : IShareRepository.IShareByUrlListener {
                override fun onSuccess(shareByUrlVO: Shareby_urlVO?) {
                    _isCreatingLinkState.value = false
                    _isLinkSharedState.value = true
                    this@ShareManagementViewModel.shareByUrlVO = shareByUrlVO
                    shareByUrlVO?.shareUrl?.let {
                        _shareLink.value = it
                        _isLinkSharedState.value = _shareLink.value != ""
                        onShowLinkSettingsBtnClick()
                    }
                }

                override fun onFailed(error: String?) {
                    _isCreatingLinkState.value = false
                    _isLinkSharedState.value = false
                    showSnackbar.value = error
                }
            })
    }

    fun copyLinkToClipboard() {

        val clipboard = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText(
            appContext.getString(R.string.share_management_share_link), _shareLink.value
        )
        clipboard.setPrimaryClip(clip)
        //showMessage.value = appContext.getString(R.string.share_management_link_copied)
    }

    fun cleanUrlRegex(url: String): String {
        return url.replace(Regex("^https?://(www\\.)?"), "")
    }


    fun onShareLinkBtnClick() {
        onShareLinkRequest.value = shareLink.value.toString()
    }

    fun onShowLinkSettingsBtnClick() {
        areLinkSettingsVisible.value = true
    }

    fun onHideLinkSettingsBtnClick() {
        areLinkSettingsVisible.value = false
    }

    fun onDefaultAccessRoleBtnClick() {
        showAccessRolesForLink.value = shareByUrlVO
    }

    fun onSharePreviewChanged(checked: Boolean) {
        this.sharePreview.value = checked
        saveChanges()
    }

    fun onAutoApproveChanged(checked: Boolean) {
        this.autoApprove.value = checked
        saveChanges()
    }

    fun onMaxUsesChanged(maxUses: Editable) {
        this.maxUses.value = maxUses.toString().trim { it <= ' ' }
    }

    fun onKeyboardDoneBtnClick(view: View, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            saveChanges()
            return false
        }
        return false
    }

    fun onExpirationDateClick() {
        saveChanges()
        showDatePicker.call()
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        expirationDate.value = "$year-${month + 1}-$day"
        saveChanges()
    }

    fun onRevokeLinkBtnClick() {
        onRevokeLinkRequest.call()
    }

    fun deleteShareLink() {
        if (_isCreatingLinkState.value != null && _isCreatingLinkState.value!!) {
            return
        }

        shareByUrlVO?.let {
            _isCreatingLinkState.value = true
            shareRepository.modifyShareLink(
                it,
                ShareRequestType.DELETE,
                object : IResponseListener {
                    override fun onSuccess(message: String?) {
                        _isCreatingLinkState.value = false
                        this@ShareManagementViewModel.shareByUrlVO = null
                        _shareLink.value = ""
                        _isLinkSharedState.value = false
                    }

                    override fun onFailed(error: String?) {
                        _isCreatingLinkState.value = false
                        showSnackbar.value = error
                    }
                })
        }
    }

    private fun saveChanges() {
        if (_isCreatingLinkState.value != null && _isCreatingLinkState.value!!) {
            return
        }
        shareByUrlVO?.let {
            it.previewToggle = if (sharePreview.value == false) 0 else 1
            it.autoApproveToggle = if (autoApprove.value == false) 0 else 1
            it.maxUses = if (maxUses.value.isNullOrBlank()) 0 else maxUses.value!!.toInt()
            it.expiresDT = expirationDate.value

            _isCreatingLinkState.value = true
            shareRepository.modifyShareLink(it, ShareRequestType.UPDATE,
                object : IResponseListener {
                    override fun onSuccess(message: String?) {
                        _isCreatingLinkState.value = false
                        showSnackbar.value = message
                    }

                    override fun onFailed(error: String?) {
                        _isCreatingLinkState.value = false
                        showSnackbar.value = error
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
                sharedWithLabelTxt.value =
                    appContext.getString(R.string.record_options_shared_with, shares.size)
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
        sharedWithLabelTxt.value =
            appContext.getString(R.string.record_options_shared_with, shares.size)
    }

    fun onAccessRoleUpdated(accessRole: AccessRole) {
        defaultAccessRole.value = accessRole
    }

    fun getShares(): List<Share> = shares

    fun getPendingShares(): List<Share> = pendingShares

    fun getRecord(): Record = record

    fun getSharesSize(): MutableLiveData<Int> = sharesSize

    fun getPendingSharesSize(): MutableLiveData<Int> = pendingSharesSize

    fun getAreLinkSettingsVisible(): MutableLiveData<Boolean> = areLinkSettingsVisible

    fun getSharePreview(): MutableLiveData<Boolean> = sharePreview

    fun getAutoApprove(): MutableLiveData<Boolean> = autoApprove

    fun getMaxUses(): MutableLiveData<String> = maxUses

    fun getAccessRole(): MutableLiveData<AccessRole> = defaultAccessRole

    fun getExpirationDate(): MutableLiveData<String> = expirationDate

    fun getShowAccessRolesForLink(): LiveData<Shareby_urlVO> = showAccessRolesForLink

    fun getShowDatePicker(): LiveData<Void?> = showDatePicker

    fun getShowSnackbar(): LiveData<String> = showSnackbar

    fun getShowSnackbarSuccess(): LiveData<String> = showSnackbarSuccess

    fun getOnShareLinkRequest(): LiveData<String> = onShareLinkRequest

    fun getSharedWithLabelTxt(): MutableLiveData<String> = sharedWithLabelTxt

    fun getOnRevokeLinkRequest(): LiveData<Void?> = onRevokeLinkRequest

    fun getShowAccessRolesForShare(): LiveData<Share> = showAccessRolesForShare

    fun getOnShareApproved(): MutableLiveData<Share> = onShareApproved

    fun getOnShareDenied(): MutableLiveData<Share> = onShareDenied
}
