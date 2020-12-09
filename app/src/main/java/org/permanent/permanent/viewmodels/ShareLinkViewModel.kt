package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.RequestType
import org.permanent.permanent.network.models.Shareby_urlVO
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository


class ShareLinkViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private lateinit var record: Record
    private val recordName = MutableLiveData<String>()
    private val existsLink = MutableLiveData(false)
    private var shareVO: Shareby_urlVO? = null
    private val sharableLink = MutableLiveData<String>()
    private val existsArchives = MutableLiveData(false)
    private val isBusy = MutableLiveData(false)
    private val showMessage = MutableLiveData<String>()
    private val showSnackBar = MutableLiveData<String>()
    private val onRevokeLinkRequest = MutableLiveData<Void>()
    private var fileRepository: IFileRepository = FileRepositoryImpl(appContext)

    fun setRecord(record: Record) {
        this.record = record
        recordName.value = record.displayName
        checkForExistingLink(record)
        existsArchives.value = !record.shares.isNullOrEmpty()
    }

    private fun checkForExistingLink(record: Record) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        fileRepository.requestShareLink(record, RequestType.GET, object : IFileRepository.IOnShareUrlListener {
            override fun onSuccess(shareVO: Shareby_urlVO?) {
                isBusy.value = false
                existsLink.value = shareVO?.shareUrl != null
                this@ShareLinkViewModel.shareVO = shareVO
                shareVO?.shareUrl?.let {
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
        return existsArchives
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

    fun getOnRevokeLinkRequest(): LiveData<Void> {
        return onRevokeLinkRequest
    }

    fun onGetLinkBtnClick() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        fileRepository.requestShareLink(record, RequestType.GENERATE, object : IFileRepository.IOnShareUrlListener {
            override fun onSuccess(shareVO: Shareby_urlVO?) {
                isBusy.value = false
                existsLink.value = shareVO?.shareUrl != null
                this@ShareLinkViewModel.shareVO = shareVO
                shareVO?.shareUrl?.let {
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

    fun onManageLinkBtnClick() {
    }

    fun onRevokeLinkBtnClick() {
        onRevokeLinkRequest.value = onRevokeLinkRequest.value
    }

    fun deleteShareLink() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        shareVO?.let {
            isBusy.value = true
            fileRepository.deleteShareLink(it, object : IFileRepository.IOnResponseListener {
                override fun onSuccess(message: String?) {
                    isBusy.value = false
                    existsLink.value = false
                    this@ShareLinkViewModel.shareVO = null
                    sharableLink.value = ""
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    showMessage.value = error
                }
            })
        }
    }
}