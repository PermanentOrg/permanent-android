package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.Editable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.myFiles.RecordListener
import org.permanent.permanent.ui.myFiles.SortType
import java.util.*

class RecordSearchViewModel(application: Application) : ObservableAndroidViewModel(application),
    RecordListener {

    private var searchJob: Job? = null
    private val isBusy = MutableLiveData(false)
    private val folderName = MutableLiveData<String>()
    private val isRoot = MutableLiveData(true)
    private val currentSearchQuery = MutableLiveData<String>()
    private val existsFiles = MutableLiveData(false)
    private val showMessage = SingleLiveEvent<String>()
    private val onRecordsRetrieved = SingleLiveEvent<List<Record>>()
    private val onFileViewRequest = SingleLiveEvent<ArrayList<Record>>()
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)
    private var folderPathStack: Stack<Record> = Stack()

    fun onSearchQueryTextChanged(query: Editable) {
        val queryString = query.toString()
        currentSearchQuery.value = queryString
        if (queryString.length < MIN_CHARS_FOR_SEARCH) {
            searchJob?.cancel()
            onRecordsRetrieved.value = ArrayList<Record>()
            isRoot.value = true
        } else if (queryString.length >= MIN_CHARS_FOR_SEARCH) searchDebounced(queryString)
    }

    private fun searchDebounced(searchText: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DELAY_MILLIS)
            requestSearchResults(searchText)
        }
    }

    private fun requestSearchResults(query: String?) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        if (!query.isNullOrEmpty()) {
            isBusy.value = true
            fileRepository.searchRecord(
                query,
                object : IFileRepository.IOnRecordsRetrievedListener {

                    override fun onSuccess(recordVOs: List<RecordVO>?) {
                        isBusy.value = false
                        isRoot.value = true
//                    existsResults.value = !recordVOs.isNullOrEmpty()
                        recordVOs?.let { onRecordsRetrieved.value = getRecords(it) }
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        showMessage.value = error
                    }
                })
        }
    }

    private fun getRecords(recordVOs: List<RecordVO>): List<Record> {
        val records = ArrayList<Record>()
        for (recordVO in recordVOs) {
            records.add(Record(recordVO))
        }
        return records
    }

    override fun onRecordClick(record: Record) {
        if (record.isProcessing) {
            return
        }

        if (record.type == RecordType.FOLDER) {
            folderPathStack.push(record)
            loadChildRecordsOf(record)
        } else onFileViewRequest.value = getFilesForViewing(onRecordsRetrieved.value, record)

    }

    private fun loadChildRecordsOf(record: Record) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        val archiveNr = record.archiveNr
        val folderLinkId = record.folderLinkId
        if (archiveNr != null && folderLinkId != null) {
            isBusy.value = true
            fileRepository.getChildRecordsOf(archiveNr,
                folderLinkId,
                SortType.NAME_ASCENDING.toBackendString(),
                object : IFileRepository.IOnRecordsRetrievedListener {
                    override fun onSuccess(recordVOs: List<RecordVO>?) {
                        isBusy.value = false
                        isRoot.value = false
                        folderName.value = record.displayName
//                        existsResults.value = !recordVOs.isNullOrEmpty()
                        recordVOs?.let { onRecordsRetrieved.value = getRecords(recordVOs) }
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        showMessage.value = error
                    }
                })
        }
    }

    override fun onRecordOptionsClick(record: Record) {}

    override fun onRecordDeleteClick(record: Record) {}

    private fun getFilesForViewing(
        allRecords: List<Record>?,
        recordToDisplayFirst: Record
    ): ArrayList<Record> {
        val files = ArrayList<Record>()
        allRecords?.let {
            for (record in it) {
                if (record.type == RecordType.FILE) {
                    record.displayFirstInCarousel = record.id == recordToDisplayFirst.id
                    files.add(record)
                }
            }
        }
        return files
    }

    fun onBackBtnClick() {
        // Popping the record of the current folder
        folderPathStack.pop()
        if (folderPathStack.isEmpty()) requestSearchResults(currentSearchQuery.value)
        else loadChildRecordsOf(folderPathStack.peek())
    }

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getFolderName(): MutableLiveData<String> = folderName

    fun getExistsFiles(): MutableLiveData<Boolean> = existsFiles

    fun getIsRoot(): MutableLiveData<Boolean> = isRoot

    fun getCurrentSearchQuery(): MutableLiveData<String> = currentSearchQuery

    fun getOnShowMessage(): MutableLiveData<String> = showMessage

    fun getOnRecordsRetrieved(): MutableLiveData<List<Record>> = onRecordsRetrieved

    fun getOnFileViewRequest(): MutableLiveData<ArrayList<Record>> = onFileViewRequest

    companion object {
        private const val MIN_CHARS_FOR_SEARCH = 3
        private const val SEARCH_DELAY_MILLIS = 100L
    }
}
