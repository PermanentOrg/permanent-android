package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.permanent.permanent.PermanentApplication
import org.permanent.permanent.R
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.ui.RelocationIslandState
import org.permanent.permanent.ui.myFiles.RelocationType

abstract class SelectionViewModel(application: Application) : RelocationViewModel(application) {

    private val appContext = application.applicationContext
    val isSelectionMode = MutableLiveData(false)
    val areAllSelected = MutableLiveData(false)
    val selectBtnText = MutableLiveData(application.getString(R.string.button_select))
    val selectedRecords = MutableLiveData<MutableList<Record>>(ArrayList())
    val selectedRecordsSize = MutableLiveData(0)
    private val expandIslandRequest = SingleLiveEvent<Void>()

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
    }

    protected fun showActionIsland() {
        showActionIsland.value = true
        getExpandIslandRequest().call()
        viewModelScope.launch {
            delay(DELAY_TO_POPULATE_ISLAND_MILLIS)
            relocationIslandState.value = RelocationIslandState.SELECTION
        }
    }

    fun onSelectionCopyBtnClick() {
        setRelocationModeForMultipleRecords(RelocationType.COPY)
    }

    fun onSelectionMoveBtnClick() {
        setRelocationModeForMultipleRecords(RelocationType.MOVE)
    }

    private fun setRelocationModeForMultipleRecords(type: RelocationType) {
        isSelectionMode.value = false
        isRelocationMode.value = true
        relocationType.value = type
        relocationIslandState.value = RelocationIslandState.CONFIRMATION
        // This is how it is the icon determined
        if (selectedRecordsSize.value == 1) recordToRelocate.value = selectedRecords.value?.get(0)
        else recordToRelocate.value = null
    }

    fun onSelectionOptionsBtnClick() {

    }

    fun onPasteOrMoveBtnClick() {
        PermanentApplication.instance.relocateData = null
        getShrinkIslandRequest().call()
        relocationIslandState.value = RelocationIslandState.PROCESSING
        val recordsToRelocate =
            if (recordToRelocate.value != null) mutableListOf(recordToRelocate.value!!) else selectedRecords.value
        val folderLinkId = currentFolder.value?.getFolderIdentifier()?.folderLinkId
        val relocationTypeValue = relocationType.value
        if (!recordsToRelocate.isNullOrEmpty() && folderLinkId != null && relocationTypeValue != null) {
            fileRepository.relocateRecords(recordsToRelocate, folderLinkId, relocationTypeValue,
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

    fun getExpandIslandRequest(): SingleLiveEvent<Void> = expandIslandRequest

    companion object {
        const val DELAY_TO_POPULATE_ISLAND_MILLIS = 400L
    }
}