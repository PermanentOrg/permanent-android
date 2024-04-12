package org.permanent.permanent.viewmodels

import android.app.Application
import kotlinx.coroutines.flow.MutableStateFlow
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.bytesToHumanReadableString

class EditFileNamesViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)

    val uiState = MutableStateFlow(EditFileNamesUIState())

    private var records: MutableList<Record> = mutableListOf()
    fun setRecords(records: ArrayList<Record>) {
        this.records.addAll(records)
        records.firstOrNull()?.let {
            updateRecordThumb(it.thumbURL200)
            updateFileName(it.displayName)
            updateRecordsCount(records.count())
            it.fileData?.size?.let { size -> updateFileSize(size) }
        }
    }

    private fun updateRecordThumb(thumb: String?) {
        uiState.value = uiState.value.copy(firstRecordThumb = thumb)
    }

    private fun updateFileName(name: String?) {
        uiState.value = uiState.value.copy(fileName = name)
    }

    fun updateError(errorMessage: String?) {
        uiState.value = uiState.value.copy(errorMessage = errorMessage)
    }

    private fun updateRecordsCount(count: Int) {
        uiState.value = uiState.value.copy(recordsNumber = count)
    }

    private fun updateFileSize(size: Long) {
        val sizeString = bytesToHumanReadableString(size)
        uiState.value = uiState.value.copy(recordSize = sizeString)

    }

    private fun triggerCloseScreen() {
        uiState.value = uiState.value.copy(shouldClose = true)
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

    fun append(text: String, before: Boolean) {
        val name = records.firstOrNull()?.displayName
        if(text.isNotEmpty()) {
            var newName = name
            if(before) {
                newName = text + newName
            } else {
                newName += text
            }
            uiState.value = uiState.value.copy(fileName = newName)
        } else {
            uiState.value = uiState.value.copy(fileName = name)
        }
    }

    fun applyChanges(findText: String, replaceText: String) {
        var containsString = false
        for(record in records) {
            if(record.displayName?.contains(findText) == true) {
                containsString = true
            }
            val newName = record.displayName?.replace(findText, replaceText)
            record.displayName = newName
        }
        if(containsString) {
            applyChanges()
        } else {
            updateError("No matching files found.  Please try again.")
        }
    }

    fun applyChanges(text: String, before: Boolean) {
        for(record in records) {
            var newName = record.displayName
            if(before) {
                newName = text + newName
            } else {
                newName += text
            }
            record.displayName = newName
        }
        applyChanges()
    }

    fun toggleLoading() {
        val currentState = uiState.value
        uiState.value = currentState.toggleIsBusy()
    }

    private fun applyChanges() {
        toggleLoading()
        fileRepository.updateMultipleRecords(records = records,
            isFolderRecordType = false,
            object : IResponseListener {
            override fun onSuccess(message: String?) {
                toggleLoading()
                triggerCloseScreen()
            }

            override fun onFailed(error: String?) {
                toggleLoading()
                error?.let {
                    updateError(it)
                }
            }
        })
    }
}

data class EditFileNamesUIState(
    val isBusy: Boolean = false,
    val firstRecordThumb: String? = null,
    val fileName: String? = null,
    val errorMessage: String? = null,
    val shouldClose: Boolean = false,
    val recordsNumber: Int = 0,
    val recordSize: String? = null
) {
    fun toggleIsBusy(): EditFileNamesUIState {
        return copy(isBusy = !isBusy)
    }
}