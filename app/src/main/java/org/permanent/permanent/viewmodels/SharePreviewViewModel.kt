package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Folder
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.Share
import org.permanent.permanent.models.Status
import org.permanent.permanent.network.models.ShareVO
import org.permanent.permanent.network.models.Shareby_urlVO
import org.permanent.permanent.repositories.IShareRepository
import org.permanent.permanent.repositories.ShareRepositoryImpl
import org.permanent.permanent.ui.shares.PreviewState

class SharePreviewViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private lateinit var urlToken: String
    private var archiveImageURL = MutableLiveData<String>()
    private var recordDisplayName = MutableLiveData<String>()
    private var accountDisplayName = MutableLiveData<String>()
    private var archiveDisplayName = MutableLiveData<String>()
    private val previewCurrentState = MutableLiveData(PreviewState.NO_ACCESS)
    private val isBusy = MutableLiveData<Boolean>()
    private val onRecordsRetrieved = SingleLiveEvent<List<Record>>()
    private val showMessage = MutableLiveData<String>()
    private var shareRepository: IShareRepository = ShareRepositoryImpl(application)

    fun getArchiveImageURL(): MutableLiveData<String> {
        return archiveImageURL
    }

    fun getRecordDisplayName(): MutableLiveData<String> {
        return recordDisplayName
    }

    fun getAccountDisplayName(): MutableLiveData<String> {
        return accountDisplayName
    }

    fun getArchiveDisplayName(): MutableLiveData<String> {
        return archiveDisplayName
    }

    fun getVisibleButton(): MutableLiveData<PreviewState> {
        return previewCurrentState
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getOnRecordsRetrieved(): MutableLiveData<List<Record>> {
        return onRecordsRetrieved
    }

    fun getShowMessage(): LiveData<String> {
        return showMessage
    }

    // -- request approval button sa apara doar daca autoApprove e false
    // si daca requestu e denied sau pending o sa apara doar textul, nu buton (edited)
    // si la ios, daca se poate cu blur-ul in functie de sharePreview true sau false
    // si cazul cand linku e expirat sau peste nr of uses
    fun checkShareLink(urlToken: String) {
        this.urlToken = urlToken
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        shareRepository.checkShareLink(urlToken, object : IShareRepository.IShareByUrlListener {
            override fun onSuccess(shareByUrlVO: Shareby_urlVO?) {
                isBusy.value = false

                val shareVO = shareByUrlVO?.ShareVO

                if (shareByUrlVO != null) {
                    archiveImageURL.value = shareByUrlVO.ArchiveVO?.thumbURL500
                    accountDisplayName.value = "Shared by ${shareByUrlVO.AccountVO?.fullName}"
                    archiveDisplayName.value = "From the ${shareByUrlVO.ArchiveVO?.fullName} Archive"

                    if (shareVO != null) {
                        val share = Share(shareVO)

                        if (share.status == Status.PENDING) {
                            previewCurrentState.value = PreviewState.AWAITING_ACCESS
                        } else { // Status.OK
                            previewCurrentState.value = PreviewState.ACCESS_GRANTED
                        }
                    } else {
                        previewCurrentState.value = PreviewState.NO_ACCESS
                    }
                    when {
                        shareByUrlVO.RecordVO != null -> {
                            recordDisplayName.value = shareByUrlVO.RecordVO?.displayName
                            onRecordsRetrieved.value = listOf(Record(shareByUrlVO.RecordVO!!))
                        }
                        shareByUrlVO.FolderVO != null -> {
                            recordDisplayName.value = shareByUrlVO.FolderVO?.displayName
                            onRecordsRetrieved.value = Folder(shareByUrlVO.FolderVO!!).records
                        }
                    }
                } else {
                    //  The share link you've tried to use is expired or deleted.
                    //
                    //Please check the URL and try again.
                    previewCurrentState.value = PreviewState.ERROR
                    // TODO: treat case
                }
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showMessage.value = error
                // TODO: treat case
            }
        })
    }

    fun onRequestAccessBtnClick() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        shareRepository.requestShareAccess(urlToken, object : IShareRepository.IShareListener {
            override fun onSuccess(shareVO: ShareVO?) {
                if (shareVO != null && Share(shareVO).status == Status.OK) {
                    previewCurrentState.value = PreviewState.ACCESS_GRANTED
                } else {
                    previewCurrentState.value = PreviewState.AWAITING_ACCESS
                }
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showMessage.value = error
            }
        })
    }

    fun onViewInArchiveBtnClick() {
        // TODO: treat case
    }

    fun onOkBtnClick() {
        // TODO: treat case
    }
}
