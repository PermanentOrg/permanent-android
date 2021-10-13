package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.Constants
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.myFiles.SortType
import java.util.*

class SharedXMeViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private lateinit var lifecycleOwner: LifecycleOwner
    val isRoot = MutableLiveData(true)
    private val isListViewMode = MutableLiveData(true)
    private val folderName = MutableLiveData(Constants.MY_FILES_FOLDER)
    private val isBusy = MutableLiveData(false)
    private val showMessage = SingleLiveEvent<String>()
    var existsShares = MutableLiveData(false)
    private var folderPathStack: Stack<Record> = Stack()
    private val onRecordsRetrieved = SingleLiveEvent<MutableList<Record>>()
    private val onRootSharesNeeded = SingleLiveEvent<Void>()
    private val onChangeViewMode = SingleLiveEvent<Boolean>()
    private val onFileViewRequest = SingleLiveEvent<Record>()
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)

    fun setLifecycleOwner(lifecycleOwner: LifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner
    }

    fun setIsListViewMode(isListViewMode: Boolean) {
        this.isListViewMode.value = isListViewMode
    }

    fun onRecordClick(record: Record) {
        if (record.type == RecordType.FOLDER) {
            folderPathStack.push(record)
            loadFilesOf(record)
        } else {
            onFileViewRequest.value = record
        }
    }

    private fun loadFilesOf(record: Record) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        val archiveNr = record.archiveNr
        val folderLinkId = record.folderLinkId
        if (archiveNr != null && folderLinkId != null) {
            isBusy.value = true
            fileRepository.getChildRecordsOf(archiveNr, folderLinkId,
                SortType.NAME_ASCENDING.toBackendString(),
                object : IFileRepository.IOnRecordsRetrievedListener {
                    override fun onSuccess(recordVOs: List<RecordVO>?) {
                        isBusy.value = false
                        isRoot.value = false
                        folderName.value = record.displayName
                        existsShares.value = !recordVOs.isNullOrEmpty()
                        recordVOs?.let { onRecordsRetrieved.value = getRecords(recordVOs) }
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        showMessage.value = error
                    }
                })
        }
    }

    private fun getRecords(recordVOs: List<RecordVO>): MutableList<Record> {
        val records = ArrayList<Record>()
        for (recordVO in recordVOs) {
            records.add(Record(recordVO))
        }
        return records
    }

    fun onBackBtnClick() {
        // This is the record of the current folder but we need his parent
        folderPathStack.pop()
        if (folderPathStack.isEmpty()) {
            onRootSharesNeeded.call()
        } else {
            val previousFolder = folderPathStack.pop()
            folderPathStack.push(previousFolder)
            loadFilesOf(previousFolder)
        }
    }

    fun onViewModeBtnClick() {
        isListViewMode.value = !isListViewMode.value!!
        onChangeViewMode.value = isListViewMode.value
    }

    fun getIsRoot(): MutableLiveData<Boolean> = isRoot

    fun getIsListViewMode(): MutableLiveData<Boolean> = isListViewMode

    fun getFolderName(): MutableLiveData<String> = folderName

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getShowMessage(): LiveData<String> = showMessage

    fun getOnRecordsRetrieved(): LiveData<MutableList<Record>> = onRecordsRetrieved

    fun getOnRootSharesNeeded(): LiveData<Void> = onRootSharesNeeded

    fun getOnChangeViewMode(): SingleLiveEvent<Boolean> = onChangeViewMode

    fun getOnFileViewRequest(): LiveData<Record> = onFileViewRequest
}
