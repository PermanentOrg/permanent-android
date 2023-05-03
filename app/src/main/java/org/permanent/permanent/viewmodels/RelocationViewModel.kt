package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.permanent.permanent.PermanentApplication
import org.permanent.permanent.models.NavigationFolder
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.RelocationIslandState
import org.permanent.permanent.ui.myFiles.RelocationType

abstract class RelocationViewModel(application: Application) :
    ObservableAndroidViewModel(application) {

    val relocationIslandState = MutableLiveData(RelocationIslandState.BLANK)
    val isRelocationMode = MutableLiveData(false)
    val relocationType = MutableLiveData<RelocationType>()
    val recordToRelocate = MutableLiveData<Record>()
    var currentFolder = MutableLiveData<NavigationFolder>()
    val existsFiles = MutableLiveData(false)
    private val shrinkIslandRequest = SingleLiveEvent<Void>()
    val onNewTemporaryFile = SingleLiveEvent<Record>()
    protected val showMessage = SingleLiveEvent<String>()
    var fileRepository: IFileRepository = FileRepositoryImpl(application)

    fun setRelocationMode(relocationPair: Pair<Record, RelocationType>) {
        PermanentApplication.instance.relocateData = relocationPair

        recordToRelocate.value = relocationPair.first
        relocationType.value = relocationPair.second
        isRelocationMode.value = true
        viewModelScope.launch {
            delay(DELAY_TO_POPULATE_ISLAND_MILLIS)
            relocationIslandState.value = RelocationIslandState.CONFIRMATION
        }
    }

    fun onPasteBtnClick() {
        PermanentApplication.instance.relocateData = null
        shrinkIslandRequest.call()
        relocationIslandState.value = RelocationIslandState.PROCESSING
        val recordValue = recordToRelocate.value
        val folderLinkId = currentFolder.value?.getFolderIdentifier()?.folderLinkId
        val relocationTypeValue = relocationType.value
        if (recordValue != null && folderLinkId != null && relocationTypeValue != null) {
            fileRepository.relocateRecord(recordValue, folderLinkId, relocationTypeValue,
                object : IResponseListener {
                    override fun onSuccess(message: String?) {
                        relocationIslandState.value = RelocationIslandState.DONE
                        onNewTemporaryFile.value = recordToRelocate.value
                        existsFiles.value = true
                        viewModelScope.launch {
                            delay(MILLIS_TO_SHOW_RELOCATION_DONE)
                            isRelocationMode.value = false
                            relocationIslandState.value = RelocationIslandState.BLANK
                        }
                    }

                    override fun onFailed(error: String?) {
                        isRelocationMode.value = false
                        relocationIslandState.value = RelocationIslandState.BLANK
                        error?.let { showMessage.value = it }
                    }
                })
        }
    }

    fun onCancelRelocationBtnClick() {
        PermanentApplication.instance.relocateData = null
        shrinkIslandRequest.call()
        isRelocationMode.value = false
        relocationIslandState.value = RelocationIslandState.BLANK
    }

    fun onSelectionCopyBtnClick() {

    }

    fun onSelectionMoveBtnClick() {

    }

    fun onSelectionOptionsBtnClick() {

    }

    fun getShrinkIslandRequest(): SingleLiveEvent<Void> = shrinkIslandRequest

    companion object {
        const val MILLIS_TO_SHOW_RELOCATION_DONE = 1000L
        const val DELAY_TO_POPULATE_ISLAND_MILLIS = 1000L
    }
}