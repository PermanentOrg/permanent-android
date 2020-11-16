package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.PermissionsHelper

class FileOptionsViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val onRequestWritePermission = SingleLiveEvent<Void>()
    private val onFileDownloadRequest = MutableLiveData<Void>()

    fun onDownloadBtnClick() {
        if (!PermissionsHelper().hasWriteStoragePermission(appContext)) {
            onRequestWritePermission.value = onRequestWritePermission.value
        } else {
            startFileDownload()
        }
    }

    fun getOnRequestWritePermission(): MutableLiveData<Void> {
        return onRequestWritePermission
    }

    fun getOnFileDownloadRequest(): MutableLiveData<Void> {
        return onFileDownloadRequest
    }

    fun onWritePermissionGranted() {
        startFileDownload()
    }

    private fun startFileDownload() {
        onFileDownloadRequest.value = onFileDownloadRequest.value
    }

    fun onCopyBtnClick() {
    }

    fun onMoveBtnClick() {
    }

    fun onPublishBtnClick() {
    }

    fun onDeleteBtnClick() {
    }

    fun onEditBtnClick() {
    }

    fun onShareBtnClick() {
    }
}