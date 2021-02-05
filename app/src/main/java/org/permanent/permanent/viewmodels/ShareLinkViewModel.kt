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


class ShareLinkViewModel(application: Application
) : ObservableAndroidViewModel(application), ShareListener {

    private val appContext = application.applicationContext
    private lateinit var record: Record
    private val recordName = MutableLiveData<String>()
    private val existsLink = MutableLiveData(false)
    private var shareByUrlVO: Shareby_urlVO? = null
    private val sharableLink = MutableLiveData<String>()
    private val existsShares = MutableLiveData(false)
    private val isBusy = MutableLiveData(false)
    private val showMessage = MutableLiveData<String>()
    private val showSnackBar = MutableLiveData<String>()
    private val onManageLinkRequest = MutableLiveData<ShareByUrl>()
    private val onRevokeLinkRequest = MutableLiveData<Void>()
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
                    showMessage.value = error
                }
            })
    }

    fun getName(): MutableLiveData<String> {
        return recordName
    }

    fun getExistsLink(): MutableLiveData<Boolean> {
        return existsLink
    }

    fun getSharableLink(): MutableLiveData<String> {
        return sharableLink
    }

    fun getExistsArchives(): MutableLiveData<Boolean> {
        return existsShares
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getShowMessage(): LiveData<String> {
        return showMessage
    }

    fun getShowSnackBar(): LiveData<String> {
        return showSnackBar
    }

    fun getOnManageLinkRequest(): LiveData<ShareByUrl> {
        return onManageLinkRequest
    }

    fun getOnRevokeLinkRequest(): LiveData<Void> {
        return onRevokeLinkRequest
    }

    fun getOnShareDenied(): MutableLiveData<Share> {
        return onShareDenied
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
                    showMessage.value = error
                }
            })
    }

    fun onCopyLinkBtnClick() {
        val clipboard = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText(
            appContext.getString(R.string.share_link_share_link_title), sharableLink.value)
        clipboard.setPrimaryClip(clip)
        showSnackBar.value = appContext.getString(R.string.share_link_link_copied)
    }

    fun onAdvancedOptionsBtnClick() {
        onManageLinkRequest.value = shareByUrlVO?.getShareByUrl()
    }

    fun onRevokeLinkBtnClick() {
        onRevokeLinkRequest.value = onRevokeLinkRequest.value
    }

    fun deleteShareLink() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        shareByUrlVO?.let {
            isBusy.value = true
            shareRepository.modifyShareLink(it, ShareRequestType.DELETE, object : IResponseListener {
                override fun onSuccess(message: String?) {
                    isBusy.value = false
                    existsLink.value = false
                    this@ShareLinkViewModel.shareByUrlVO = null
                    sharableLink.value = ""
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    showMessage.value = error
                }
            })
        }
    }

    override fun onApproveClick(share: Share) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        shareRepository.approveShare(share, object : IResponseListener {
            override fun onSuccess(message: String?) {
                isBusy.value = false
                showMessage.value = message
                share.status.value = Status.OK // This hides the Approve and Deny buttons
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showMessage.value = error
            }
        })
    }

    override fun onDenyClick(share: Share) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        shareRepository.denyShare(share, object : IResponseListener {
            override fun onSuccess(message: String?) {
                isBusy.value = false
                showMessage.value = message
                onShareDenied.value = share // Removes share from adapter
                record.shares?.remove(share)
                existsShares.value = !record.shares.isNullOrEmpty()
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showMessage.value = error
            }
        })
    }
}