package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.permanent.permanent.PermanentApplication
import org.permanent.permanent.models.NavigationFolder
import org.permanent.permanent.models.Record
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.RelocationIslandState
import org.permanent.permanent.ui.myFiles.RelocationType

abstract class RelocationViewModel(application: Application) :
    ObservableAndroidViewModel(application) {

    val relocationIslandState = MutableLiveData(RelocationIslandState.BLANK)
    val isRelocationMode = MutableLiveData(false)
    val showActionIsland = MutableLiveData(false)
    val recordsToRelocate = MutableLiveData<MutableList<Record>>()
    val relocationType = MutableLiveData<RelocationType>()
    var currentFolder = MutableLiveData<NavigationFolder>()
    val existsFiles = MutableLiveData(false)
    private val shrinkIslandRequest = SingleLiveEvent<Void>()
    val onNewTemporaryFiles = SingleLiveEvent<MutableList<Record>>()
    protected val showMessage = SingleLiveEvent<String>()
    var fileRepository: IFileRepository = FileRepositoryImpl(application)

    fun setRelocationMode(relocationPair: Pair<MutableList<Record>, RelocationType>) {
        PermanentApplication.instance.relocateData = relocationPair

        recordsToRelocate.value = relocationPair.first
        relocationType.value = relocationPair.second
        isRelocationMode.value = true
        viewModelScope.launch {
            delay(DELAY_TO_POPULATE_ISLAND_MILLIS)
            relocationIslandState.value = RelocationIslandState.CONFIRMATION
        }
    }

    open fun onCancelRelocationBtnClick() {
        PermanentApplication.instance.relocateData = null
        isRelocationMode.value = false
        shrinkAndHideActionIsland()
    }

    protected fun shrinkAndHideActionIsland() {
        shrinkIslandRequest.call()
        waitAndHideActionIsland()
    }

    protected fun waitAndHideActionIsland() {
        viewModelScope.launch {
            delay(SelectionViewModel.DELAY_TO_POPULATE_ISLAND_MILLIS)
            relocationIslandState.value = RelocationIslandState.BLANK
            showActionIsland.value = false
        }
    }

    fun getShrinkIslandRequest(): SingleLiveEvent<Void> = shrinkIslandRequest

    companion object {
        const val DELAY_TO_POPULATE_ISLAND_MILLIS = 1000L
    }
}