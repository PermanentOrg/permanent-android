package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import android.text.Editable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.permanent.permanent.R
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.models.Tag
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.repositories.ITagRepository
import org.permanent.permanent.repositories.TagRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.myFiles.RecordListener
import org.permanent.permanent.ui.myFiles.SortType
import java.util.*
import kotlin.collections.ArrayList

class RecordSearchViewModel(application: Application) : ObservableAndroidViewModel(application),
    RecordListener {

    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private var searchJob: Job? = null
    private val isBusy = MutableLiveData(false)
    private val folderName = MutableLiveData<String>()
    private val isRoot = MutableLiveData(true)
    val currentSearchQuery = MutableLiveData<String>()
    private val existsTags = MutableLiveData(false)
    private val existsRecords = MutableLiveData(false)
    private val showMessage = SingleLiveEvent<String>()
    private val tagsTitle =
        MutableLiveData(application.getString(R.string.record_search_tags_title, "0"))
    private val allTags = ArrayList<Tag>()
    private val onVisibleTagsReady = SingleLiveEvent<ArrayList<Tag>>()
    private val onRecordsRetrieved = SingleLiveEvent<List<Record>>()
    private val onFileViewRequest = SingleLiveEvent<ArrayList<Record>>()
    private var tagRepository: ITagRepository = TagRepositoryImpl(application)
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)
    private var folderPathStack: Stack<Record> = Stack()

    init {
        requestTagsForCurrentArchive()
    }

    private fun requestTagsForCurrentArchive() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        tagRepository.getTagsByArchive(prefsHelper.getCurrentArchiveId(), object : IDataListener {

            override fun onSuccess(dataList: List<Datum>?) {
                isBusy.value = false
                dataList?.let {
                    for (data in it) {
                        data.TagVO?.let { tagVO -> allTags.add(Tag(tagVO)) }
                    }
                    allTags.sortBy { tag -> tag.name.lowercase() }
                    existsTags.value = allTags.isNotEmpty()
                    onVisibleTagsReady.value = allTags
                }
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showMessage.value = error
            }
        })
    }

    fun onSearchQueryTextChanged(query: Editable) {
        currentSearchQuery.value = query.toString()
        if (currentSearchQuery.value.isNullOrEmpty()) {
            searchJob?.cancel()
            isRoot.value = true
            existsRecords.value = false
        }
        searchAndFilterDebounced()
    }

    private fun searchAndFilterDebounced() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DELAY_MILLIS)
            searchRecords()
            filterTags(currentSearchQuery.value)
        }
    }

    fun searchRecords() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        val query = currentSearchQuery.value
        val checkedTags = getCheckedTags(onVisibleTagsReady.value)
        if (!query.isNullOrEmpty() || checkedTags.isNotEmpty()) {
            isBusy.value = true
            fileRepository.searchRecords(
                query,
                checkedTags,
                object : IFileRepository.IOnRecordsRetrievedListener {

                    override fun onSuccess(recordVOs: List<RecordVO>?) {
                        isBusy.value = false
                        isRoot.value = true
                        existsRecords.value = !recordVOs.isNullOrEmpty()
                        recordVOs?.let { onRecordsRetrieved.value = getRecords(it) }
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        showMessage.value = error
                    }
                })
        } else {
            isRoot.value = true
            existsRecords.value = false
        }
    }

    private fun getCheckedTags(visibleTags: ArrayList<Tag>?): ArrayList<Tag> {
        val checkedTags = ArrayList<Tag>()
        if (visibleTags != null) {
            for (tag in visibleTags) {
                if (tag.isCheckedOnLocal) {
                    checkedTags.add(tag)
                }
            }
        }
        tagsTitle.value =
            appContext.getString(R.string.record_search_tags_title, checkedTags.size.toString())
        return checkedTags
    }

    private fun filterTags(searchQuery: String?) {
        if (searchQuery.isNullOrEmpty()) {
            existsTags.value = allTags.isNotEmpty()
            if (allTags.isNotEmpty()) onVisibleTagsReady.value = allTags
        } else {
            val filteredTags = ArrayList<Tag>()
            for (tag in allTags) {
                if (tag.name.lowercase(Locale.ROOT)
                        .contains(searchQuery.lowercase(Locale.ROOT)) || tag.isCheckedOnLocal
                ) {
                    filteredTags.add(tag)
                }
            }
            existsTags.value = filteredTags.isNotEmpty()
            if (filteredTags.isNotEmpty()) onVisibleTagsReady.value = filteredTags
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
                        existsRecords.value = !recordVOs.isNullOrEmpty()
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
        if (folderPathStack.isEmpty()) searchRecords()
        else loadChildRecordsOf(folderPathStack.peek())
    }

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getFolderName(): MutableLiveData<String> = folderName

    fun getTagsTitle(): MutableLiveData<String> = tagsTitle

    fun getExistsTags(): MutableLiveData<Boolean> = existsTags

    fun getExistsRecords(): MutableLiveData<Boolean> = existsRecords

    fun getIsRoot(): MutableLiveData<Boolean> = isRoot

    fun getOnShowMessage(): MutableLiveData<String> = showMessage

    fun getOnVisibleTagsReady(): MutableLiveData<ArrayList<Tag>> = onVisibleTagsReady

    fun getOnRecordsRetrieved(): MutableLiveData<List<Record>> = onRecordsRetrieved

    fun getOnFileViewRequest(): MutableLiveData<ArrayList<Record>> = onFileViewRequest

    companion object {
        private const val SEARCH_DELAY_MILLIS = 300L
    }
}
