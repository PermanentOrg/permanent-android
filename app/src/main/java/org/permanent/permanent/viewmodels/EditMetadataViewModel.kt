package org.permanent.permanent.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditMetadataViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private var records: List<Record> = emptyList()
    private var commonDescription: String = ""
    private var someFilesHaveDescription = MutableLiveData(false)
    val showError = MutableLiveData<String>()
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)

    fun setRecords(records: ArrayList<Record>) {
        this.records = records
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
                    if (someFilesHaveDescription.value == false) {
                        someFilesHaveDescription.value =
                            !record.fileData?.description.isNullOrBlank()
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    showError.value = t.message
                }
            })
        } else Log.e("EditMetadataViewModel", "folderLinkId or recordId is null")
    }

    fun applyNewDescriptionToAllRecords(description: String) {
        if (commonDescription != description) {
            for (record in this.records) {
                record.fileData?.let { fileData ->
                    fileData.description = description
                    fileRepository.updateRecord(fileData, object : IResponseListener {
                        override fun onSuccess(message: String?) {
                            Log.d("EditMetadataViewModel", "Description for record: ${record.displayName} was updated")
                        }

                        override fun onFailed(error: String?) {
                            error?.let { showError.value = it }
                        }
                    })
                }
            }
        }
    }

    fun getRecords() = records

    fun getCommonDescription(): String = commonDescription

    fun getSomeFilesHaveDescription(): MutableLiveData<Boolean> = someFilesHaveDescription
}
