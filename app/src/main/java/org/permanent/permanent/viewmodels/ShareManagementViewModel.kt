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
import okhttp3.internal.trimSubstring
import org.permanent.permanent.R
import org.permanent.permanent.models.*
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
    private val existsShares = MutableLiveData(false)
    private val sharePreview = MutableLiveData(false)
    private val autoApprove = MutableLiveData(false)
    private val maxUses = MutableLiveData("0")
    private val defaultAccessRole = MutableLiveData(AccessRole.VIEWER)
    private val expirationDate = MutableLiveData<String>()
    private val showDatePicker = SingleLiveEvent<Void>()
    private val areLinkSettingsVisible = MutableLiveData(false)
    private val isBusy = MutableLiveData(false)
    private val showSnackbar = MutableLiveData<String>()
    private val showSnackbarSuccess = MutableLiveData<String>()
    private val onShareLinkRequest = SingleLiveEvent<String>()
    private val onLinkSettingsRequest = MutableLiveData<ShareByUrl>()
    private val onRevokeLinkRequest = SingleLiveEvent<Void>()
    private val onShowShareOptionsRequest = SingleLiveEvent<Share>()
    private val onShareDenied = SingleLiveEvent<Share>()
    private var shareRepository: IShareRepository = ShareRepositoryImpl(appContext)

    fun setRecord(record: Record) {
        this.record = record
        recordName.value = record.displayName
        existsShares.value = !record.shares.isNullOrEmpty()
    }

    fun setShareLink(shareByUrlVO: Shareby_urlVO?) {
        if (shareByUrlVO == null) {
            checkForExistingLink(record)
        } else {
            init(shareByUrlVO)
        }
    }

    private fun init(shareByUrlVO: Shareby_urlVO) {
        this.shareByUrlVO = shareByUrlVO
        this.shareLink.value = shareByUrlVO.shareUrl ?: ""
        sharePreview.value = shareByUrlVO.previewToggle == 1
        autoApprove.value = shareByUrlVO.autoApproveToggle == 1
        maxUses.value = shareByUrlVO.maxUses.toString()
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

    fun onAccessRoleBtnClick() {

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

    fun onCopyLinkBtnClick() {
        val clipboard = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText(
            appContext.getString(R.string.share_management_share_link), shareLink.value
        )
        clipboard.setPrimaryClip(clip)
        showSnackbarSuccess.value = appContext.getString(R.string.share_management_link_copied)
    }

    fun onLinkSettingsBtnClick() {
        shareByUrlVO?.let { onLinkSettingsRequest.value = ShareByUrl(it) }
    }

    fun onRemoveLinkBtnClick() {
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

    override fun onOptionsClick(share: Share) {
        onShowShareOptionsRequest.value = share
    }

    override fun onApproveClick(share: Share) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        shareRepository.updateShare(share, object : IResponseListener {
            override fun onSuccess(message: String?) {
                isBusy.value = false
                showSnackbarSuccess.value = message
                share.status.value = Status.OK // This hides the Approve and Deny buttons
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
                showSnackbarSuccess.value = message
                onShareDenied.value = share // Removes share from adapter
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showSnackbar.value = error
            }
        })
    }

    fun getRecord(): Record = record

    fun getRecordName(): MutableLiveData<String> = recordName

    fun getShareLink(): MutableLiveData<String> = shareLink

    fun getExistsShares(): MutableLiveData<Boolean> = existsShares

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getAreLinkSettingsVisible(): MutableLiveData<Boolean> = areLinkSettingsVisible

    fun getSharePreview(): MutableLiveData<Boolean> = sharePreview

    fun getAutoApprove(): MutableLiveData<Boolean> = autoApprove

    fun getMaxUses(): MutableLiveData<String> = maxUses

    fun getAccessRole(): MutableLiveData<AccessRole> = defaultAccessRole

    fun getExpirationDate(): MutableLiveData<String> = expirationDate

    fun getShowDatePicker(): LiveData<Void> = showDatePicker

    fun getShowSnackbar(): LiveData<String> = showSnackbar

    fun getShowSnackbarSuccess(): LiveData<String> = showSnackbarSuccess

    fun getOnShareLinkRequest(): LiveData<String> = onShareLinkRequest

    fun getOnLinkSettingsRequest(): LiveData<ShareByUrl> = onLinkSettingsRequest

    fun getOnRevokeLinkRequest(): LiveData<Void> = onRevokeLinkRequest

    fun getOnShowShareOptionsRequest(): LiveData<Share> = onShowShareOptionsRequest

    fun getOnShareDenied(): MutableLiveData<Share> = onShareDenied
}
