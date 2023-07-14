package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Archive
import org.permanent.permanent.models.Folder
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.Share
import org.permanent.permanent.models.Status
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.network.models.ShareVO
import org.permanent.permanent.network.models.Shareby_urlVO
import org.permanent.permanent.repositories.ArchiveRepositoryImpl
import org.permanent.permanent.repositories.IArchiveRepository
import org.permanent.permanent.repositories.IShareRepository
import org.permanent.permanent.repositories.ShareRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.myFiles.RecordListener
import org.permanent.permanent.ui.shares.PreviewState

class SharePreviewViewModel(application: Application) : ObservableAndroidViewModel(application),
    RecordListener {

    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private lateinit var urlToken: String
    private var recordIdToView: Int? = null
    private var archiveThumbURL = MutableLiveData<String>()
    private var recordDisplayName = MutableLiveData<String>()
    private var accountDisplayName = MutableLiveData<String>()
    private var archiveDisplayName = MutableLiveData<String>()
    private val currentState = MutableLiveData(PreviewState.NO_ACCESS)
    private val currentArchiveThumb = MutableLiveData<String>()
    private val currentArchiveName = MutableLiveData<String>()
    private val isCurrentArchiveDefault = MutableLiveData(false)
    private var showChangeArchiveButton = MutableLiveData(false)
    private val onRecordsRetrieved = SingleLiveEvent<List<Record>>()
    private val onChangeArchive = SingleLiveEvent<Void?>()
    private val onViewInArchive = SingleLiveEvent<Int?>()
    private val onNavigateUp = SingleLiveEvent<Void?>()
    private val isBusy = MutableLiveData<Boolean>()
    private val errorMessage = MutableLiveData<String>()
    private var shareRepository: IShareRepository = ShareRepositoryImpl(application)
    private var archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)

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
                archiveThumbURL.value = shareByUrlVO?.ArchiveVO?.thumbURL200
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

                isBusy.value = true
                archiveRepository.getAllArchives(object : IDataListener {
                    override fun onSuccess(dataList: List<Datum>?) {
                        isBusy.value = false
                        if (!dataList.isNullOrEmpty()) {
                            var notPendingArchives = 0

                            for (datum in dataList) {
                                val archive = Archive(datum.ArchiveVO)
                                if (archive.status != Status.PENDING) notPendingArchives++
                            }

                            showChangeArchiveButton.value = notPendingArchives > 1
                        }
                    }
                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        errorMessage.value = error
                    }
                })

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
                currentArchiveThumb.value = prefsHelper.getCurrentArchiveThumbURL()
                currentArchiveName.value = prefsHelper.getCurrentArchiveFullName()
                isCurrentArchiveDefault.value =
                    prefsHelper.getCurrentArchiveId() == prefsHelper.getDefaultArchiveId()
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                currentState.value = PreviewState.ERROR
                errorMessage.value = error
            }
        })
    }

    fun onChangeArchiveBtnClick() {
        onChangeArchive.call()
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

    override fun onRecordClick(record: Record) {}

    override fun onRecordOptionsClick(record: Record) {}

    override fun onRecordCheckBoxClick(record: Record) {}

    override fun onRecordDeleteClick(record: Record) {}

    fun getArchiveThumbURL(): MutableLiveData<String> = archiveThumbURL

    fun getRecordDisplayName(): MutableLiveData<String> = recordDisplayName

    fun getAccountDisplayName(): MutableLiveData<String> = accountDisplayName

    fun getArchiveDisplayName(): MutableLiveData<String> = archiveDisplayName

    fun getCurrentState(): MutableLiveData<PreviewState> = currentState

    fun getCurrentArchiveThumb(): MutableLiveData<String> = currentArchiveThumb

    fun getCurrentArchiveName(): MutableLiveData<String> = currentArchiveName

    fun getIsCurrentArchiveDefault(): MutableLiveData<Boolean> = isCurrentArchiveDefault

    fun getShowChangeArchiveButton(): MutableLiveData<Boolean> = showChangeArchiveButton

    fun getOnRecordsRetrieved(): MutableLiveData<List<Record>> = onRecordsRetrieved

    fun getOnChangeArchive(): MutableLiveData<Void?> = onChangeArchive

    fun getOnViewInArchive(): MutableLiveData<Int?> = onViewInArchive

    fun getOnNavigateUp(): MutableLiveData<Void?> = onNavigateUp

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getErrorMessage(): LiveData<String> = errorMessage
}
