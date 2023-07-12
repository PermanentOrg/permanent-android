package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.ui.Workspace

class ChooseFolderViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val onPrivateFilesSelected = SingleLiveEvent<Void?>()
    private val onSharedFilesSelected = SingleLiveEvent<Void?>()
    private val onPublicFilesSelected = SingleLiveEvent<Void?>()
    private val onCancelRequest = SingleLiveEvent<Void?>()

    fun onCancelBtnClick() {
        onCancelRequest.call()
    }

    fun onWorkspaceClick(workspace: Workspace) {
        when (workspace) {
            Workspace.SHARES -> onSharedFilesSelected.call()
            Workspace.PUBLIC_FILES -> onPublicFilesSelected.call()
            else -> onPrivateFilesSelected.call()
        }
    }

    fun getOnPrivateFilesSelected(): MutableLiveData<Void?> = onPrivateFilesSelected
    fun getOnSharedFilesSelected(): MutableLiveData<Void?> = onSharedFilesSelected
    fun getOnPublicFilesSelected(): MutableLiveData<Void?> = onPublicFilesSelected
    fun getOnCancelRequest(): MutableLiveData<Void?> = onCancelRequest
}
