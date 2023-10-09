package org.permanent.permanent.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.models.LegacyContact
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.repositories.ILegacyPlanningRepository
import org.permanent.permanent.repositories.LegacyPlanningRepositoryImpl
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditMetadataViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val appContext = application.applicationContext

    private val onLegacyContactReady = MutableLiveData<List<LegacyContact>>()
    private var legacyPlanningRepository: ILegacyPlanningRepository =
        LegacyPlanningRepositoryImpl(appContext)
    private var records: List<Record> = emptyList()
    private var description: String = ""
    private var someFilesHaveDescription = MutableLiveData(false)
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
                }
            })
        } else Log.e("EditMetadataViewModel", "folderLinkId or recordId is null")
    }

    fun getRecords() = records

    fun getDescription(): String = description

    fun getSomeFilesHaveDescription(): MutableLiveData<Boolean> = someFilesHaveDescription

    fun setDescription(description: String) {
        this.description = description
    }
}
