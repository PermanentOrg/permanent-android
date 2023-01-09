package org.permanent.permanent.viewmodels

import android.app.Application
import android.app.DatePickerDialog
import android.text.Editable
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.DatePicker
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import okhttp3.internal.trimSubstring
import org.permanent.permanent.R
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.Share
import org.permanent.permanent.models.Status
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.ShareRequestType
import org.permanent.permanent.network.models.Shareby_urlVO
import org.permanent.permanent.repositories.IShareRepository
import org.permanent.permanent.repositories.ShareRepositoryImpl
import org.permanent.permanent.ui.shareManagement.ShareListener


class ShareManagementViewModel(application: Application) : ObservableAndroidViewModel(application),
    ShareListener, DatePickerDialog.OnDateSetListener {

    private val appContext = application.applicationContext
    private lateinit var record: Record
    private val recordName = MutableLiveData<String>()
    private var shareByUrlVO: Shareby_urlVO? = null
    private val shareLink = MutableLiveData("")
    private var shares = mutableListOf<Share>()
    private var pendingShares = mutableListOf<Share>()
    private val sharesSize = MutableLiveData(0)
    private val pendingSharesSize = MutableLiveData(0)
    private val sharePreview = MutableLiveData(false)
    private val autoApprove = MutableLiveData(false)
    private val maxUses = MutableLiveData("0")
    private val defaultAccessRole = MutableLiveData(AccessRole.VIEWER)
    private val expirationDate = MutableLiveData<String>()
    private val showDatePicker = SingleLiveEvent<Void>()
    private val sharedWithLabelTxt = MutableLiveData<String>()
    private val areLinkSettingsVisible = MutableLiveData(false)
    private val isBusy = MutableLiveData(false)
    private val showSnackbar = MutableLiveData<String>()
    private val showSnackbarSuccess = MutableLiveData<String>()
    private val onShareLinkRequest = SingleLiveEvent<String>()
    private val onRevokeLinkRequest = SingleLiveEvent<Void>()
    private val showAccessRolesForLink = SingleLiveEvent<Shareby_urlVO>()
    private val showAccessRolesForShare = SingleLiveEvent<Share>()
    private val onShareApproved = SingleLiveEvent<Share>()
    private val onShareDenied = SingleLiveEvent<Share>()
    private var shareRepository: IShareRepository = ShareRepositoryImpl(appContext)

    fun setRecord(record: Record) {
        this.record = record
        recordName.value = record.displayName
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
        this.shareLink.value = shareByUrlVO.shareUrl ?: ""
        sharePreview.value = shareByUrlVO.previewToggle == 1
        autoApprove.value = shareByUrlVO.autoApproveToggle == 1
        maxUses.value = (shareByUrlVO.maxUses ?: 0).toString()
        defaultAccessRole.value = AccessRole.createFromBackendString(shareByUrlVO.defaultAccessRole)
        shareByUrlVO.expiresDT.let { expirationDate.value = it?.trimSubstring() }
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
                    shareByUrlVO?.let { init(it) }
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    showSnackbar.value = error
                }
            })
    }

    fun onCreateLinkBtnClick() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        shareRepository.requestShareLink(record, ShareRequestType.GENERATE,
            object : IShareRepository.IShareByUrlListener {
                override fun onSuccess(shareByUrlVO: Shareby_urlVO?) {
                    isBusy.value = false
                    this@ShareManagementViewModel.shareByUrlVO = shareByUrlVO
                    shareByUrlVO?.shareUrl?.let {
                        shareLink.value = it
                        onShowLinkSettingsBtnClick()
                    }
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    showSnackbar.value = error
                }
            })
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
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        shareByUrlVO?.let {
            isBusy.value = true
            shareRepository.modifyShareLink(
                it,
                ShareRequestType.DELETE,
                object : IResponseListener {
                    override fun onSuccess(message: String?) {
                        isBusy.value = false
                        this@ShareManagementViewModel.shareByUrlVO = null
                        shareLink.value = ""
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        showSnackbar.value = error
                    }
                })
        }
    }

    private fun saveChanges() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        shareByUrlVO?.let {
            it.previewToggle = if (sharePreview.value == false) 0 else 1
            it.autoApproveToggle = if (autoApprove.value == false) 0 else 1
            it.maxUses = if (maxUses.value.isNullOrBlank()) 0 else maxUses.value!!.toInt()
            it.expiresDT = expirationDate.value

            isBusy.value = true
            shareRepository.modifyShareLink(it, ShareRequestType.UPDATE,
                object : IResponseListener {
                    override fun onSuccess(message: String?) {
                        isBusy.value = false
                        showSnackbar.value = message
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        showSnackbar.value = error
                    }
                })
        }
    }

    override fun onEditClick(share: Share) {
        showAccessRolesForShare.value = share
    }

    override fun onApproveClick(share: Share) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        shareRepository.updateShare(share, object : IResponseListener {
            override fun onSuccess(message: String?) {
                isBusy.value = false
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
                isBusy.value = false
                showSnackbar.value = error
            }
        })
    }

    override fun onDenyClick(share: Share) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        shareRepository.deleteShare(share, object : IResponseListener {
            override fun onSuccess(message: String?) {
                isBusy.value = false
                pendingShares.remove(share)
                pendingSharesSize.value = pendingShares.size
                showSnackbarSuccess.value = message
                onShareDenied.value = share // Removes share from adapter
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showSnackbar.value = error
            }
        })
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

    fun getRecordName(): MutableLiveData<String> = recordName

    fun getShareLink(): MutableLiveData<String> = shareLink

    fun getSharesSize(): MutableLiveData<Int> = sharesSize

    fun getPendingSharesSize(): MutableLiveData<Int> = pendingSharesSize

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getAreLinkSettingsVisible(): MutableLiveData<Boolean> = areLinkSettingsVisible

    fun getSharePreview(): MutableLiveData<Boolean> = sharePreview

    fun getAutoApprove(): MutableLiveData<Boolean> = autoApprove

    fun getMaxUses(): MutableLiveData<String> = maxUses

    fun getAccessRole(): MutableLiveData<AccessRole> = defaultAccessRole

    fun getExpirationDate(): MutableLiveData<String> = expirationDate

    fun getShowAccessRolesForLink(): LiveData<Shareby_urlVO> = showAccessRolesForLink

    fun getShowDatePicker(): LiveData<Void> = showDatePicker

    fun getShowSnackbar(): LiveData<String> = showSnackbar

    fun getShowSnackbarSuccess(): LiveData<String> = showSnackbarSuccess

    fun getOnShareLinkRequest(): LiveData<String> = onShareLinkRequest

    fun getSharedWithLabelTxt(): MutableLiveData<String> = sharedWithLabelTxt

    fun getOnRevokeLinkRequest(): LiveData<Void> = onRevokeLinkRequest

    fun getShowAccessRolesForShare(): LiveData<Share> = showAccessRolesForShare

    fun getOnShareApproved(): MutableLiveData<Share> = onShareApproved

    fun getOnShareDenied(): MutableLiveData<Share> = onShareDenied
}
