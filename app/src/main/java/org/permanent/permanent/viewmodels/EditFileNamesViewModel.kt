package org.permanent.permanent.viewmodels

import android.app.Application
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository

class EditFileNamesViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)

    val uiState = MutableStateFlow(EditFileNamesUIState())

    private var records: MutableList<Record> = mutableListOf()
    fun setRecords(records: ArrayList<Record>) {
        this.records.addAll(records)
        records.firstOrNull()?.let {
            updateRecordThumb(it.thumbURL200)
            updateFileName(it.displayName)
        }
    }

    private fun updateRecordThumb(thumb: String?) {
        uiState.value = uiState.value.copy(firstRecordThumb = thumb)
    }

    private fun updateFileName(name: String?) {
        uiState.value = uiState.value.copy(fileName = name)
    }

    fun replace(findText: String, replaceText: String) {
        val name = records.firstOrNull()?.displayName
        if(findText.isNotEmpty()) {
            val newName = name?.replace(findText, replaceText)
            uiState.value = uiState.value.copy(fileName = newName)
        } else {
            uiState.value = uiState.value.copy(fileName = name)
        }
    }

    fun applyChanges(findText: String, replaceText: String) {
        for(record in records) {
            val newName = record.displayName?.replace(findText, replaceText)
            record.displayName = newName
        }
        applyChanges()
    }

    private fun applyChanges() {
        fileRepository.updateMultipleRecords(records = records, object : IResponseListener {
            override fun onSuccess(message: String?) {
//                isBusy.value = false
//                commonDescription = inputDescription
                Log.d("EditMetadataViewModel", "Description for records was updated")
            }

            override fun onFailed(error: String?) {
//                isBusy.value = false
//                error?.let { showError.value = it }
            }
        })
    }
}

data class EditFileNamesUIState(
    val firstRecordThumb: String? = null,
    val fileName: String? = null
)