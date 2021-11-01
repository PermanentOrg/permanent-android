package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.Share
import org.permanent.permanent.models.ShareByUrl
import org.permanent.permanent.models.Status
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.ShareRequestType
import org.permanent.permanent.network.models.Shareby_urlVO
import org.permanent.permanent.repositories.IShareRepository
import org.permanent.permanent.repositories.ShareRepositoryImpl
import org.permanent.permanent.ui.myFiles.linkshare.ShareListener


class ShareLinkViewModel(application: Application) : ObservableAndroidViewModel(application),
    ShareListener {

    private val appContext = application.applicationContext
    private lateinit var record: Record
    private val recordName = MutableLiveData<String>()
    private val existsLink = MutableLiveData(false)
    private var shareByUrlVO: Shareby_urlVO? = null
    private val sharableLink = MutableLiveData<String>()
    private val existsShares = MutableLiveData(false)
    private val isBusy = MutableLiveData(false)
    private val showSnackbar = MutableLiveData<String>()
    private val showSnackbarSuccess = MutableLiveData<String>()
    private val onLinkSettingsRequest = MutableLiveData<ShareByUrl>()
    private val onRevokeLinkRequest = SingleLiveEvent<Void>()
    private val onShowShareOptionsRequest = SingleLiveEvent<Share>()
    private val onShareDenied = SingleLiveEvent<Share>()
    private var shareRepository: IShareRepository = ShareRepositoryImpl(appContext)

    fun setRecord(record: Record) {
        this.record = record
        checkForExistingLink(record)
        recordName.value = record.displayName
        existsShares.value = !record.shares.isNullOrEmpty()
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
                    existsLink.value = shareByUrlVO?.shareUrl != null
                    this@ShareLinkViewModel.shareByUrlVO = shareByUrlVO
                    shareByUrlVO?.shareUrl?.let {
                        sharableLink.value = it
                    }
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    showSnackbar.value = error
                }
            })
    }

    fun onGetLinkBtnClick() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        shareRepository.requestShareLink(record, ShareRequestType.GENERATE,
            object : IShareRepository.IShareByUrlListener {
                override fun onSuccess(shareByUrlVO: Shareby_urlVO?) {
                    isBusy.value = false
                    existsLink.value = shareByUrlVO?.shareUrl != null
                    this@ShareLinkViewModel.shareByUrlVO = shareByUrlVO
                    shareByUrlVO?.shareUrl?.let {
                        sharableLink.value = it
                    }
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    showSnackbar.value = error
                }
            })
    }

    fun onCopyLinkBtnClick() {
        val clipboard = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText(
            appContext.getString(R.string.share_link_share_link_title), sharableLink.value
        )
        clipboard.setPrimaryClip(clip)
        showSnackbarSuccess.value = appContext.getString(R.string.share_link_link_copied)
    }

    fun onLinkSettingsBtnClick() {
        onLinkSettingsRequest.value = shareByUrlVO?.getShareByUrl()
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
                        existsLink.value = false
                        this@ShareLinkViewModel.shareByUrlVO = null
                        sharableLink.value = ""
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

    fun getName(): MutableLiveData<String> = recordName

    fun getExistsLink(): MutableLiveData<Boolean> = existsLink

    fun getSharableLink(): MutableLiveData<String> = sharableLink

    fun getExistsShares(): MutableLiveData<Boolean> = existsShares

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getShowSnackbar(): LiveData<String> = showSnackbar

    fun getShowSnackbarSuccess(): LiveData<String> = showSnackbarSuccess

    fun getOnLinkSettingsRequest(): LiveData<ShareByUrl> = onLinkSettingsRequest

    fun getOnRevokeLinkRequest(): LiveData<Void> = onRevokeLinkRequest

    fun getOnShowShareOptionsRequest(): LiveData<Share> = onShowShareOptionsRequest

    fun getOnShareDenied(): MutableLiveData<Share> = onShareDenied
}
