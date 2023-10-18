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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditMetadataViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private var records: MutableList<Record> = mutableListOf()
    private var allTags = MutableLiveData<List<Tag>>()
    private var commonTags: MutableList<Tag> = mutableListOf()
    private var initialDescription: String = ""
    private var commonDescription: String = ""
    private var showWarningSomeFilesHaveDescription = MutableLiveData(false)
    val showError = MutableLiveData<String>()
    private var fileDataSize = 0
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
            fileRepository.getRecord(folderLinkId, recordId).enqueue(object : Callback<ResponseVO> {

                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    record.fileData = response.body()?.getFileData()
                    fileDataSize++
                    if (fileDataSize == records.size) {
                        checkForCommonDescription()
                        checkForCommonTags()
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
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
    }

    fun applyNewDescriptionToAllRecords(inputDescription: String) {
        if (commonDescription != inputDescription) {
            val fileDataList = records.map {
                it.fileData?.description = inputDescription
                it.fileData
            }

            fileRepository.updateRecords(fileDataList, object : IResponseListener {
                override fun onSuccess(message: String?) {
                    commonDescription = inputDescription
                    Log.d("EditMetadataViewModel", "Description for records was updated")
                }

                override fun onFailed(error: String?) {
                    error?.let { showError.value = it }
                }
            })
        }
    }

    fun getRecords() = records

    fun getAllTags() = allTags

    fun getCommonTags() = commonTags

    fun getCommonDescription(): String = commonDescription

    fun getSomeFilesHaveDescription(): MutableLiveData<Boolean> = showWarningSomeFilesHaveDescription
}
