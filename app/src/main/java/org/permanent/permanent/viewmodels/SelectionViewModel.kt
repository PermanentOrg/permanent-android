package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.permanent.permanent.PermanentApplication
import org.permanent.permanent.R
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.ui.RelocationIslandState
import org.permanent.permanent.ui.myFiles.ModificationType

abstract class SelectionViewModel(application: Application) : RelocationViewModel(application) {

    private val appContext = application.applicationContext
    val isSelectionMode = MutableLiveData(false)
    val areAllSelected = MutableLiveData(false)
    val selectBtnText = MutableLiveData(application.getString(R.string.button_select))
    val selectedRecordsSize = MutableLiveData(0)
    private val selectedRecords = MutableLiveData<MutableList<Record>>(ArrayList())
    private val expandIslandRequest = SingleLiveEvent<Void?>()
    private val showSelectionOptionsRequest = SingleLiveEvent<Pair<Int, Boolean>>()
    private val showEditMetadataRequest = SingleLiveEvent<MutableList<Record>>()
    private val refreshCurrentFolderRequest = SingleLiveEvent<Void?>()

    fun onSelectBtnClick() {
        isSelectionMode.value = true
        selectBtnText.value = appContext.getString(R.string.button_select_all)
    }

    fun onRecordChecked(record: Record) {
        if (record.isChecked?.value == true) {
            selectedRecords.value?.add(record)
            selectedRecordsSize.value = selectedRecordsSize.value!! + 1
            if (selectedRecordsSize.value == 1) showActionIsland()
        } else {
            selectedRecords.value?.remove(record)
            selectedRecordsSize.value = selectedRecordsSize.value!! - 1
            areAllSelected.value = false
            if (selectedRecordsSize.value == 0) shrinkAndHideActionIsland()
        }
    }

    fun onSelectAllRecords(allRecords: List<Record>) {
        if (selectedRecordsSize.value == allRecords.size) {
            // If there are all selected, we deselect
            deselectAllRecords()
            shrinkAndHideActionIsland()
        } else { // If there are none selected, we select them all
            areAllSelected.value = true
            showActionIsland()
            for (record in allRecords) {
                record.isChecked?.value = true
            }
            selectedRecords.value?.clear() // We remove those selected one by one first
            selectedRecords.value?.addAll(allRecords)
            selectedRecordsSize.value = selectedRecords.value!!.size
        }
    }

    private fun deselectAllRecords() {
        areAllSelected.value = false
        for (record in selectedRecords.value!!) {
            record.isChecked?.value = false
        }
        selectedRecords.value?.clear()
        selectedRecordsSize.value = selectedRecords.value!!.size
    }

    fun onClearBtnClick() {
        isSelectionMode.value = false
        selectBtnText.value = appContext.getString(R.string.button_select)
        deselectAllRecords()
        shrinkAndHideActionIsland()
    }

    override fun onCancelRelocationBtnClick() {
        super.onCancelRelocationBtnClick()
        selectBtnText.value = appContext.getString(R.string.button_select)
        deselectAllRecords()
        PermanentApplication.instance.relocateData = null
    }

    private fun showActionIsland() {
        showActionIsland.value = true
        getExpandIslandRequest().call()
        viewModelScope.launch {
            delay(DELAY_TO_POPULATE_ISLAND_MILLIS)
            relocationIslandState.value = RelocationIslandState.SELECTION
        }
    }

    fun onSelectionModifyBtnClick(type: ModificationType) {
        when (type) {
            ModificationType.DELETE -> {
                deleteSelectedRecords()
            }

            ModificationType.EDIT -> {
                editMetadataForSelectedRecords()
            }

            else -> {
                setRelocationModeForMultipleRecords(type)
            }
        }
    }

    private fun setRelocationModeForMultipleRecords(type: ModificationType) {
        isSelectionMode.value = false
        isRelocationMode.value = true
        modificationType.value = type
        relocationIslandState.value = RelocationIslandState.CONFIRMATION
        recordsToRelocate.value = selectedRecords.value
        selectedRecords.value?.let { PermanentApplication.instance.relocateData = Pair(it, type) }
    }

    fun onSelectionOptionsBtnClick() {
        showSelectionOptionsRequest.value = Pair(selectedRecordsSize.value!!, isSelectionContainingFolders())
    }

    private fun isSelectionContainingFolders(): Boolean {
        val selectedRecords = selectedRecords.value
        if (!selectedRecords.isNullOrEmpty()) {
            for (record in selectedRecords) {
                if (record.type == RecordType.FOLDER) return true
            }
            return false
        }
        return true
    }

    fun onPasteOrMoveBtnClick() {
        PermanentApplication.instance.relocateData = null
        getShrinkIslandRequest().call()
        relocationIslandState.value = RelocationIslandState.PROCESSING
        val recordsToRelocate = recordsToRelocate.value
        val folderLinkId = currentFolder.value?.getFolderIdentifier()?.folderLinkId
        val relocationTypeValue = modificationType.value
        if (!recordsToRelocate.isNullOrEmpty() && folderLinkId != null && relocationTypeValue != null) {
            fileRepository.relocateRecords(
                recordsToRelocate,
                folderLinkId,
                relocationTypeValue,
                object : IResponseListener {
                    override fun onSuccess(message: String?) {
                        relocationIslandState.value = RelocationIslandState.DONE
                        onNewTemporaryFiles.value = recordsToRelocate
                        existsFiles.value = true
                        waitAndHideActionIsland()
                        viewModelScope.launch {
                            delay(DELAY_TO_POPULATE_ISLAND_MILLIS)
                            isRelocationMode.value = false
                            selectBtnText.value = appContext.getString(R.string.button_select)
                            deselectAllRecords()
                        }
                        viewModelScope.launch {
                            delay(DELAY_TO_REFRESH_MILLIS)
                            refreshCurrentFolderRequest.call()
                        }
                    }

                    override fun onFailed(error: String?) {
                        waitAndHideActionIsland()
                        viewModelScope.launch {
                            delay(DELAY_TO_POPULATE_ISLAND_MILLIS)
                            isRelocationMode.value = false
                            selectBtnText.value = appContext.getString(R.string.button_select)
                            deselectAllRecords()
                            error?.let { showMessage.value = it }
                        }
                    }
                })
        }
    }

    private fun editMetadataForSelectedRecords() {
        isSelectionMode.value = false
        getShrinkIslandRequest().call()
        val recordsToModify = selectedRecords.value
        if (!recordsToModify.isNullOrEmpty()) {
            showEditMetadataRequest.value = recordsToModify!!
            waitAndHideActionIsland()
            viewModelScope.launch {
                delay(DELAY_TO_POPULATE_ISLAND_MILLIS)
                isRelocationMode.value = false
                selectBtnText.value = appContext.getString(R.string.button_select)
                deselectAllRecords()
                refreshCurrentFolderRequest.call()
            }
        }
    }

    fun deleteSelectedRecords() {
        isSelectionMode.value = false
        getShrinkIslandRequest().call()
        relocationIslandState.value = RelocationIslandState.PROCESSING
        val recordsToRelocate = selectedRecords.value
        if (!recordsToRelocate.isNullOrEmpty()) {
            fileRepository.deleteRecords(recordsToRelocate, object : IResponseListener {
                override fun onSuccess(message: String?) {
                    relocationIslandState.value = RelocationIslandState.DONE
                    waitAndHideActionIsland()
                    viewModelScope.launch {
                        delay(DELAY_TO_POPULATE_ISLAND_MILLIS)
                        isRelocationMode.value = false
                        selectBtnText.value = appContext.getString(R.string.button_select)
                        deselectAllRecords()
                        refreshCurrentFolderRequest.call()
                    }
                }

                override fun onFailed(error: String?) {
                    waitAndHideActionIsland()
                    viewModelScope.launch {
                        delay(DELAY_TO_POPULATE_ISLAND_MILLIS)
                        isRelocationMode.value = false
                        selectBtnText.value = appContext.getString(R.string.button_select)
                        deselectAllRecords()
                        error?.let { showMessage.value = it }
                    }
                }
            })
        }
    }

    fun getExpandIslandRequest(): SingleLiveEvent<Void?> = expandIslandRequest

    fun getShowSelectionOptionsRequest(): SingleLiveEvent<Pair<Int, Boolean>> = showSelectionOptionsRequest

    fun getShowEditMetadataScreenRequest(): SingleLiveEvent<MutableList<Record>> = showEditMetadataRequest

    fun getRefreshCurrentFolderRequest(): SingleLiveEvent<Void?> = refreshCurrentFolderRequest

    companion object {
        const val DELAY_TO_POPULATE_ISLAND_MILLIS = 400L
        const val DELAY_TO_REFRESH_MILLIS = 3000L
    }
}