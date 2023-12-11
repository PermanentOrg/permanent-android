package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.PermanentApplication
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.Tag
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.ITagListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.repositories.ITagRepository
import org.permanent.permanent.repositories.TagRepositoryImpl
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditMetadataViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private var records: MutableList<Record> = mutableListOf()
    private val allTagsOfSelectedRecords =
        MutableLiveData<SnapshotStateList<Tag>>(mutableStateListOf())
    private var allTagsOfArchive = MutableLiveData<MutableList<Tag>>(mutableListOf())
    private var commonTags: MutableList<Tag> = mutableListOf()
    private var initialDescription: String = ""
    private var commonDescription: String = ""
    private var showWarningSomeFilesHaveDescription = MutableLiveData(false)
    val showError = MutableLiveData<String>()
    val showApplyAllToSelection = MutableLiveData(true)
    private val isBusy = MutableLiveData(false)
    private var fileDataSize = 0
    private var tagRepository: ITagRepository = TagRepositoryImpl(application)
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)
    val prefsHelper = PreferencesHelper(
        PermanentApplication.instance.applicationContext.getSharedPreferences(
            PREFS_NAME, Context.MODE_PRIVATE
        )
    )

    fun setRecords(records: ArrayList<Record>) {
        this.records.addAll(records)
        for (record in this.records) {
            requestFileData(record)
        }
    }

    private fun requestFileData(record: Record) {
        val folderLinkId = record.folderLinkId
        val recordId = record.recordId

        if (folderLinkId != null && recordId != null) {
            isBusy.value = true
            fileRepository.getRecord(folderLinkId, recordId).enqueue(object : Callback<ResponseVO> {

                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    isBusy.value = false
                    record.fileData = response.body()?.getFileData()
                    fileDataSize++
                    if (fileDataSize == records.size) {
                        checkForCommonDescription()
                        checkForCommonTags()
                        requestTagsForCurrentArchive()
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    isBusy.value = false
                    showError.value = t.message
                }
            })
        } else Log.e("EditMetadataViewModel", "folderLinkId or recordId is null")
    }

    private fun checkForCommonDescription() {
        for (record in records) {
            val recordDescription = record.fileData?.description
            if (!recordDescription.isNullOrBlank()) {
                initialDescription = recordDescription
                break
            }
        }

        if (initialDescription.isNotEmpty()) {
            showWarningSomeFilesHaveDescription.value = true
            for (record in records) {
                val recordDescription = record.fileData?.description
                if (initialDescription != recordDescription) {
                    commonDescription = ""
                    return
                }
            }
            // If this point is reached, all descriptions are the same
            commonDescription = initialDescription
        } else {
            showWarningSomeFilesHaveDescription.value = false
        }
    }

    private fun checkForCommonTags() {
        allTagsOfSelectedRecords.value?.addAll(
            records.flatMap { it.fileData?.tags ?: emptyList() }.toSet().toMutableList())
        allTagsOfSelectedRecords.value?.let { allTagsValue ->
            commonTags.addAll(allTagsValue)

            for (tag in allTagsValue) {
                for (record in records) {
                    if (record.fileData?.tags?.contains(tag) == false) {
                        commonTags.remove(tag)
                        break
                    }
                }
            }
        }
        for (tag in commonTags) {
            tag.isSelected.value = true
        }
        if (allTagsOfSelectedRecords.value?.size == commonTags.size) {
            showApplyAllToSelection.value = false
        }
    }

    private fun requestTagsForCurrentArchive() {
        tagRepository.getTagsByArchive(prefsHelper.getCurrentArchiveId(), object : IDataListener {

            override fun onSuccess(dataList: List<Datum>?) {
                dataList?.let {
                    for (data in it) {
                        data.TagVO?.let { tagVO -> allTagsOfArchive.value?.add(Tag(tagVO)) }
                    }
                }
            }

            override fun onFailed(error: String?) {
                error?.let { showError.value = it }
            }
        })
    }

    fun applyNewDescriptionToAllRecords(inputDescription: String) {
        if (commonDescription != inputDescription) {
            isBusy.value = true

            val fileDataList = records.map {
                it.fileData?.description = inputDescription
                it.fileData
            }
            fileRepository.updateRecords(fileDataList, object : IResponseListener {
                override fun onSuccess(message: String?) {
                    isBusy.value = false
                    commonDescription = inputDescription
                    Log.d("EditMetadataViewModel", "Description for records was updated")
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    error?.let { showError.value = it }
                }
            })
        }
    }

    fun onTagClick(tag: Tag) { // apply tag to all records
        var appliedTagToRecordsNr = 0

        isBusy.value = true
        for (record in records) {
            record.recordId?.let { recordId ->
                tagRepository.createOrLinkTags(mutableListOf(tag), recordId, object : ITagListener {

                    override fun onSuccess(createdTag: Tag?) {
                        appliedTagToRecordsNr++
                        if (appliedTagToRecordsNr == records.size) {
                            isBusy.value = false
                            commonTags.add(tag)
                            tag.isSelected.value = true
                        }
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        error?.let { showError.value = it }
                    }
                })
            }
        }
    }

    fun onTagRemoveClick(tag: Tag) { // remove tag from all records
        var removedTagFromRecordsNr = 0

        isBusy.value = true
        for (record in records) {
            record.recordId?.let {
                tagRepository.unlinkTags(mutableListOf(tag), it, object : IResponseListener {

                    override fun onSuccess(message: String?) {
                        removedTagFromRecordsNr++
                        if (removedTagFromRecordsNr == records.size) {
                            isBusy.value = false
                            allTagsOfSelectedRecords.value?.remove(tag)
                            commonTags.remove(tag)
                        }
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        error?.let { showError.value = it }
                    }
                })
            }
        }
    }

    fun onApplyAllTagsToSelectionClick() {
        allTagsOfSelectedRecords.value?.filterNot { it in commonTags }?.let { uncommonTags ->

            isBusy.value = true
            var appliedTagsToRecordsNr = 0
            for (record in records) {
                record.recordId?.let {
                    tagRepository.createOrLinkTags(uncommonTags, it, object : ITagListener {

                        override fun onSuccess(createdTag: Tag?) {
                            appliedTagsToRecordsNr++
                            if (appliedTagsToRecordsNr == records.size) {
                                isBusy.value = false
                                commonTags.addAll(uncommonTags)
                                for (tag in uncommonTags) tag.isSelected.value = true
                                showApplyAllToSelection.value = false
                            }
                        }

                        override fun onFailed(error: String?) {
                            isBusy.value = false
                            error?.let { showError.value = it }
                        }
                    })
                }
            }
        }
    }

    fun onTagsAddedToSelection(tags: List<Tag>) {
        commonTags.addAll(tags)
        allTagsOfSelectedRecords.value?.addAll(tags)
    }

    fun getRecentTags(): ArrayList<Tag> {
        val recentTags = arrayListOf<Tag>()
        allTagsOfArchive.value?.let { recentTags.addAll(it) }
        allTagsOfSelectedRecords.value?.let { recentTags.removeAll(it.toSet()) }
        recentTags.filter { it.isSelected.value == true }.map { it.isSelected.value = false }
        return recentTags
    }

    fun getIsBusy() = isBusy

    fun getRecords() = records

    fun getAllTags() = allTagsOfSelectedRecords

    fun getSomeFilesHaveDescription() = showWarningSomeFilesHaveDescription
}
