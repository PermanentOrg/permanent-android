package org.permanent.permanent.viewmodels

import android.app.Application
import kotlinx.coroutines.flow.MutableStateFlow
import org.permanent.permanent.models.Record

class EditFileNamesViewModel(application: Application) : ObservableAndroidViewModel(application) {
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
        if(findText.isNotEmpty()) {
            val name = records.firstOrNull()?.displayName
            val newName = name?.replace(findText, replaceText)
            uiState.value = uiState.value.copy(fileName = newName)
        }
    }
}

data class EditFileNamesUIState(
    val firstRecordThumb: String? = null,
    val fileName: String? = null
)