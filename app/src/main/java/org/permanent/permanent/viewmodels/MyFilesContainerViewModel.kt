package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.ui.Workspace

class MyFilesContainerViewModel(application: Application) :
    ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val title = MutableLiveData<String>()
    private val shownInWorkspace = MutableLiveData(Workspace.PUBLIC_ARCHIVES)
    private val onSaveFolderRequest = SingleLiveEvent<Void>()
    private val onCancelRequest = SingleLiveEvent<Void>()

    fun setShownInWorkspace(workspace: Workspace?) {
        shownInWorkspace.value = workspace
        if (shownInWorkspace.value == Workspace.PUBLIC_ARCHIVES) {
            title.value = appContext.getString(R.string.private_files_container_select_file_title)
        } else {
            title.value = appContext.getString(R.string.private_files_container_select_folder_title)
        }
    }

    fun onCancelBtnClick() {
        onCancelRequest.call()
    }

    fun onSaveBtnClick() {
        onSaveFolderRequest.call()
    }

    fun getTitle(): MutableLiveData<String> = title
    fun getShownInWorkspace(): MutableLiveData<Workspace> = shownInWorkspace
    fun getOnSaveFolderRequest(): MutableLiveData<Void> = onSaveFolderRequest
    fun getOnCancelRequest(): MutableLiveData<Void> = onCancelRequest
}
