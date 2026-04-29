package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.permanent.permanent.R
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.models.Share
import org.permanent.permanent.models.Status
import org.permanent.permanent.network.ILinkListener
import org.permanent.permanent.network.models.IFolderChildrenListener
import org.permanent.permanent.network.models.ShareLinkVO
import org.permanent.permanent.network.models.ShareVO
import org.permanent.permanent.network.models.Shareby_urlVO
import org.permanent.permanent.repositories.ArchiveRepositoryImpl
import org.permanent.permanent.repositories.IArchiveRepository
import org.permanent.permanent.repositories.IShareRepository
import org.permanent.permanent.repositories.ShareRepositoryImpl
import org.permanent.permanent.repositories.StelaAccountRepository
import org.permanent.permanent.repositories.StelaAccountRepositoryImpl
import org.permanent.permanent.ui.myFiles.RecordListener
import org.permanent.permanent.ui.shareManagement.compose.AccessType
import org.permanent.permanent.ui.shares.PreviewState

class SharePreviewViewModel(application: Application) : ObservableAndroidViewModel(application),
    RecordListener {

    private lateinit var urlToken: String
    private var recordIdToView: Int? = null
    private val _archiveThumbURL = MutableStateFlow("")
    private val recordDisplayName = SingleLiveEvent<String>()
    private val _rawAccountName = MutableStateFlow("")
    private val _rawArchiveName = MutableStateFlow("")
    private val _records = MutableStateFlow<List<Record>>(emptyList())
    private val _accessType = MutableStateFlow<AccessType?>(null)
    private val _currentState = MutableStateFlow(PreviewState.NO_ACCESS)
    private val currentArchiveThumb = MutableLiveData<String>()
    private val _currentArchiveName = MutableStateFlow("")
    private val isCurrentArchiveDefault = MutableLiveData(false)
    private val _showChangeArchiveButton = MutableStateFlow(false)
    private val onRecordsRetrieved = SingleLiveEvent<List<Record>>()
    private val onChangeArchive = SingleLiveEvent<Void?>()
    private val onViewInArchive = SingleLiveEvent<Int?>()
    private val onNavigateUp = SingleLiveEvent<Void?>()
    private val _isBusy = MutableStateFlow(false)
    private val errorMessage = MutableLiveData<String>()
    private var shareRepository: IShareRepository = ShareRepositoryImpl(application)
    private var archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)
    private var stelaAccountRepository: StelaAccountRepository =
        StelaAccountRepositoryImpl(application)

    fun checkShareLink(urlToken: String) {
        this.urlToken = urlToken
        if (_isBusy.value) {
            return
        }

        _isBusy.value = true
        shareRepository.checkShareLink(urlToken, object : IShareRepository.IShareByUrlListener {
            override fun onSuccess(shareByUrlVO: Shareby_urlVO?) {
                _isBusy.value = false
                // Loading data in the header
                _archiveThumbURL.value = shareByUrlVO?.ArchiveVO?.thumbnail256 ?: shareByUrlVO?.ArchiveVO?.thumbURL200 ?: ""
                _rawAccountName.value = shareByUrlVO?.AccountVO?.fullName ?: ""
                _rawArchiveName.value = "The ${shareByUrlVO?.ArchiveVO?.fullName} Archive"

                // Loading toolbar title
                when {
                    shareByUrlVO?.RecordVO != null -> {
                        recordDisplayName.value = shareByUrlVO.RecordVO?.displayName ?: ""
                    }
                    shareByUrlVO?.FolderVO != null -> {
                        recordDisplayName.value = shareByUrlVO.FolderVO?.displayName ?: ""
                    }
                }

                // Get link from Stela for accessRestrictions (determines blur)
                _isBusy.value = true
                stelaAccountRepository.getShareLink(
                    shareTokens = mutableListOf(urlToken),
                    listener = object : ILinkListener {

                        override fun onSuccess(shareLink: ShareLinkVO?) {
                            _isBusy.value = false

                            val access =
                                shareLink?.accessRestrictions?.let { AccessType.fromBackendValue(it) }

                            _accessType.value = access

                            if (access != AccessType.ANYONE_CAN_VIEW) {
                                _records.value = getFakeBlurredRecords()

                            } else if (shareByUrlVO?.RecordVO != null) {
                                _records.value = listOf(Record(shareByUrlVO))

                            } else if (shareByUrlVO?.FolderVO?.folderId != null) {
                                loadRealFolderChildren(shareByUrlVO.FolderVO?.folderId!!)
                            }
                        }

                        override fun onFailed(error: String?) {
                            _isBusy.value = false
                            _currentState.value = PreviewState.ERROR
                            errorMessage.value = error
                        }
                    })

//                _isBusy.value = true
//                archiveRepository.getAllArchives(object : IDataListener {
//                    override fun onSuccess(dataList: List<Datum>?) {
//                        _isBusy.value = false
//                        if (!dataList.isNullOrEmpty()) {
//                            var notPendingArchives = 0
//
//                            for (datum in dataList) {
//                                val archive = Archive(datum.ArchiveVO)
//                                if (archive.status != Status.PENDING) notPendingArchives++
//                            }
//
//                            _showChangeArchiveButton.value = notPendingArchives > 1
//                        }
//                    }
//
//                    override fun onFailed(error: String?) {
//                _isBusy.value = false
//                _currentState.value = PreviewState.ERROR
//                errorMessage.value = error
//                    }
//                })
//
//                // Loading data in the footer
//                val shareVO = shareByUrlVO?.ShareVO
//                if (shareVO != null) {
//                    val share = Share(shareVO)
//
//                    if (share.status.value == Status.PENDING) {
//                        // Showing 'Awaiting for Access' text
//                        _currentState.value = PreviewState.AWAITING_ACCESS
//                    } else { // Showing 'View in Archive' button
//                        _currentState.value = PreviewState.ACCESS_GRANTED
//                    }
//                } else {
//                    // Showing 'Request Access' button
//                    _currentState.value = PreviewState.NO_ACCESS
//                }
//                currentArchiveThumb.value = prefsHelper.getCurrentArchiveThumbURL()
//                _currentArchiveName.value = prefsHelper.getCurrentArchiveFullName() ?: ""
//                isCurrentArchiveDefault.value =
//                    prefsHelper.getCurrentArchiveId() == prefsHelper.getDefaultArchiveId()
            }

            override fun onFailed(error: String?) {
                _isBusy.value = false
                _currentState.value = PreviewState.ERROR
                errorMessage.value = error
            }
        })
    }

    private fun loadRealFolderChildren(folderId: Int) {
        _isBusy.value = true

        stelaAccountRepository.getFolderChildren(
            shareToken = urlToken,
            folderId = folderId,
            listener = object : IFolderChildrenListener {

                override fun onSuccess(records: List<Record>) {
                    _isBusy.value = false
                    _records.value = mapToPreviewLayout(records)
                }

                override fun onFailed(error: String?) {
                    _isBusy.value = false
                    _currentState.value = PreviewState.ERROR
                    errorMessage.value = error
                }
            }
        )
    }

    private fun mapToPreviewLayout(records: List<Record>): List<Record> {
        val folders = records.filter { it.type == RecordType.FOLDER }
        val images = records.filter { it.type == RecordType.FILE }

        // Case 1: No images → show folders only (max 4)
        if (images.isEmpty()) {
            return folders.take(4)
        }

        val result = MutableList<Record?>(4) { null }

        // Put first folder in position 0 (if exists)
        var usedFolders = 0
        if (folders.isNotEmpty()) {
            result[0] = folders[0]
            usedFolders = 1
        }

        when (images.size) {
            1 -> {
                result[3] = images[0]
            }

            2 -> {
                result[2] = images[0]
                result[3] = images[1]
            }

            else -> {
                val startIndex = if (folders.isNotEmpty()) 1 else 0
                val maxImages = 4 - startIndex

                images.take(maxImages).forEachIndexed { index, image ->
                    result[startIndex + index] = image
                }
            }
        }

        // ✅ Fill remaining empty slots with remaining folders
        val remainingFolders = folders.drop(usedFolders).iterator()

        for (i in result.indices) {
            if (result[i] == null && remainingFolders.hasNext()) {
                result[i] = remainingFolders.next()
            }
        }

        return result.filterNotNull()
    }

    fun onChangeArchiveBtnClick() {
        onChangeArchive.call()
    }

    fun onRequestAccessBtnClick() {
        if (_isBusy.value) {
            return
        }

        _isBusy.value = true
        shareRepository.requestShareAccess(urlToken, object : IShareRepository.IShareListener {
            override fun onSuccess(shareVO: ShareVO?) {
                _isBusy.value = false
                if (shareVO != null && Share(shareVO).status.value == Status.OK) {
                    _currentState.value = PreviewState.ACCESS_GRANTED
                } else {
                    _currentState.value = PreviewState.AWAITING_ACCESS
                }
            }

            override fun onFailed(error: String?) {
                _isBusy.value = false
                _currentState.value = PreviewState.ERROR
                errorMessage.value = error
            }
        })
    }

    private fun getFakeBlurredRecords(): List<Record> {
        return listOf(
            // Fake folder (top-left)
            Record(recordId = -1, folderLinkId = -1).apply {
                type = RecordType.FOLDER
                displayName = "Folder"
                isThumbBlurred = true
            },

            // Fake images
            Record(recordId = -2, folderLinkId = -1).apply {
                isThumbBlurred = true
                localDrawableRes = R.drawable.img_share_preview_tall
            },
            Record(recordId = -3, folderLinkId = -1).apply {
                isThumbBlurred = true
                localDrawableRes = R.drawable.img_share_preview_small
            },
            Record(recordId = -4, folderLinkId = -1).apply {
                isThumbBlurred = true
                localDrawableRes = R.drawable.img_share_preview_large
            }
        )
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

    val archiveThumbURL: StateFlow<String> = _archiveThumbURL.asStateFlow()

    fun getRecordDisplayName(): MutableLiveData<String> = recordDisplayName

    val currentState: StateFlow<PreviewState> = _currentState.asStateFlow()

    fun getCurrentArchiveThumb(): MutableLiveData<String> = currentArchiveThumb

    val currentArchiveName: StateFlow<String> = _currentArchiveName.asStateFlow()

    fun getIsCurrentArchiveDefault(): MutableLiveData<Boolean> = isCurrentArchiveDefault

    val showChangeArchiveButton: StateFlow<Boolean> = _showChangeArchiveButton.asStateFlow()

    fun getOnRecordsRetrieved(): MutableLiveData<List<Record>> = onRecordsRetrieved

    fun getOnChangeArchive(): MutableLiveData<Void?> = onChangeArchive

    fun getOnViewInArchive(): MutableLiveData<Int?> = onViewInArchive

    fun getOnNavigateUp(): MutableLiveData<Void?> = onNavigateUp

    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    fun getErrorMessage(): MutableLiveData<String> = errorMessage

    val rawAccountName: StateFlow<String> = _rawAccountName.asStateFlow()

    val rawArchiveName: StateFlow<String> = _rawArchiveName.asStateFlow()

    val records: StateFlow<List<Record>> = _records.asStateFlow()

    val accessType: StateFlow<AccessType?> = _accessType.asStateFlow()
}
