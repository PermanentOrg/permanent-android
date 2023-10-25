package org.permanent.permanent.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.Tag
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.repositories.ITagRepository
import org.permanent.permanent.repositories.TagRepositoryImpl
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditMetadataViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private var records: MutableList<Record> = mutableListOf()
    private var allTags = MutableLiveData<MutableList<Tag>>()
    private var commonTags: MutableList<Tag> = mutableListOf()
    private var initialDescription: String = ""
    private var commonDescription: String = ""
    private var showWarningSomeFilesHaveDescription = MutableLiveData(false)
    val showError = MutableLiveData<String>()
    private val isBusy = MutableLiveData(false)
    private var fileDataSize = 0
    private var tagRepository: ITagRepository = TagRepositoryImpl(application)
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)

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
        allTags.value = records.flatMap { it.fileData?.tags ?: emptyList() }.toSet().toMutableList()
        allTags.value?.let { allTagsValue ->
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

        for (record in records) {
            record.recordId?.let { recordId ->

                isBusy.value = true
                tagRepository.createOrLinkTags(mutableListOf(tag), recordId, object : IResponseListener {

                    override fun onSuccess(message: String?) {
                        isBusy.value = false
                        appliedTagToRecordsNr++
                        if (appliedTagToRecordsNr == records.size) {
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

        for (record in records) {
            record.recordId?.let {

                isBusy.value = true
                tagRepository.unlinkTags(mutableListOf(tag), it, object : IResponseListener {

                    override fun onSuccess(message: String?) {
                        isBusy.value = false
                        removedTagFromRecordsNr++
                        if (removedTagFromRecordsNr == records.size) {
                            allTags.value?.remove(tag)
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
        allTags.value?.filterNot { it in commonTags }?.let { uncommonTags ->
            for (record in records) {
                var appliedTagsToRecordsNr = 0
                record.recordId?.let {

                    isBusy.value = true
                    tagRepository.createOrLinkTags(uncommonTags, it, object : IResponseListener {

                        override fun onSuccess(message: String?) {
                            isBusy.value = false
                            appliedTagsToRecordsNr++
                            if (appliedTagsToRecordsNr == records.size) commonTags.addAll(uncommonTags)
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

    fun getIsBusy() = isBusy

    fun getRecords() = records

    fun getAllTags() = allTags

    fun getCommonTags() = commonTags

    fun getCommonDescription(): String = commonDescription

    fun getSomeFilesHaveDescription(): MutableLiveData<Boolean> =
        showWarningSomeFilesHaveDescription
}
