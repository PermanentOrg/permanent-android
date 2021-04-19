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
    private var recordIdToView: Int? = null
    private var archiveImageURL = MutableLiveData<String>()
    private var recordDisplayName = MutableLiveData<String>()
    private var accountDisplayName = MutableLiveData<String>()
    private var archiveDisplayName = MutableLiveData<String>()
    private val currentState = MutableLiveData(PreviewState.NO_ACCESS)
    private val isBusy = MutableLiveData<Boolean>()
    private val onRecordsRetrieved = SingleLiveEvent<List<Record>>()
    private val onViewInArchive = SingleLiveEvent<Int?>()
    private val onNavigateUp = SingleLiveEvent<Void>()
    private val errorMessage = MutableLiveData<String>()
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

    fun getCurrentState(): MutableLiveData<PreviewState> {
        return currentState
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getOnRecordsRetrieved(): MutableLiveData<List<Record>> {
        return onRecordsRetrieved
    }

    fun getOnViewInArchive(): MutableLiveData<Int?> {
        return onViewInArchive
    }

    fun getOnNavigateUp(): MutableLiveData<Void> {
        return onNavigateUp
    }

    fun getErrorMessage(): LiveData<String> {
        return errorMessage
    }

    fun checkShareLink(urlToken: String) {
        this.urlToken = urlToken
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        shareRepository.checkShareLink(urlToken, object : IShareRepository.IShareByUrlListener {
            override fun onSuccess(shareByUrlVO: Shareby_urlVO?) {
                isBusy.value = false
                // Loading data in the header
                archiveImageURL.value = shareByUrlVO?.ArchiveVO?.thumbURL500
                accountDisplayName.value = "Shared by ${shareByUrlVO?.AccountVO?.fullName}"
                archiveDisplayName.value = "From the ${shareByUrlVO?.ArchiveVO?.fullName} Archive"
                // Loading data in the list
                when {
                    shareByUrlVO?.RecordVO != null -> {
                        recordIdToView = shareByUrlVO.RecordVO?.recordId
                        recordDisplayName.value = shareByUrlVO.RecordVO?.displayName
                        onRecordsRetrieved.value = listOf(Record(shareByUrlVO))
                    }
                    shareByUrlVO?.FolderVO != null -> {
                        recordIdToView = shareByUrlVO.FolderVO?.folderId
                        recordDisplayName.value = shareByUrlVO.FolderVO?.displayName
                        onRecordsRetrieved.value = Folder(shareByUrlVO).records
                    }
                }
                // Loading data in the footer
                val shareVO = shareByUrlVO?.ShareVO
                if (shareVO != null) {
                    val share = Share(shareVO)

                    if (share.status.value == Status.PENDING) {
                        // Showing 'Awaiting for Access' text
                        currentState.value = PreviewState.AWAITING_ACCESS
                    } else { // Showing 'View in Archive' button
                        currentState.value = PreviewState.ACCESS_GRANTED
                    }
                } else {
                    // Showing 'Request Access' button
                    currentState.value = PreviewState.NO_ACCESS
                }
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                currentState.value = PreviewState.ERROR
                errorMessage.value = error
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
                isBusy.value = false
                if (shareVO != null && Share(shareVO).status.value == Status.OK) {
                    currentState.value = PreviewState.ACCESS_GRANTED
                } else {
                    currentState.value = PreviewState.AWAITING_ACCESS
                }
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                currentState.value = PreviewState.ERROR
                errorMessage.value = error
            }
        })
    }

    fun onViewInArchiveBtnClick() {
        onViewInArchive.value = recordIdToView
    }

    fun onOkBtnClick() {
        onNavigateUp.call()
    }
}
