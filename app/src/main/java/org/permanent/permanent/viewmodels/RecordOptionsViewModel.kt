package org.permanent.permanent.viewmodels

import android.app.Application
import android.os.Build
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.PermissionsHelper
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.ui.myFiles.RelocationType

class RecordOptionsViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val isBusy = MutableLiveData<Boolean>()
    private val isFolder = MutableLiveData(false)
    private val recordName = MutableLiveData<String>()
    private val onRequestWritePermission = SingleLiveEvent<Void>()
    private val onFileDownloadRequest = MutableLiveData<Void>()
    private val onRecordDeleteRequest = MutableLiveData<Void>()
    private val onRelocateRequest = MutableLiveData<RelocationType>()

    fun setRecord(record: Record?) {
        isFolder.value = record?.type == RecordType.FOLDER
        recordName.value = record?.displayName
    }

    fun onDownloadBtnClick() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
            && !PermissionsHelper().hasWriteStoragePermission(appContext)) {
            onRequestWritePermission.value = onRequestWritePermission.value
        } else {
            startFileDownload()
        }
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getIsFolder(): MutableLiveData<Boolean> {
        return isFolder
    }

    fun getName(): MutableLiveData<String> {
        return recordName
    }

    fun getOnRequestWritePermission(): MutableLiveData<Void> {
        return onRequestWritePermission
    }

    fun getOnFileDownloadRequest(): MutableLiveData<Void> {
        return onFileDownloadRequest
    }

    fun getOnRecordDeleteRequest(): MutableLiveData<Void> {
        return onRecordDeleteRequest
    }

    fun onWritePermissionGranted() {
        startFileDownload()
    }

    private fun startFileDownload() {
        onFileDownloadRequest.value = onFileDownloadRequest.value
    }

    fun onCopyBtnClick() {
        onRelocateRequest.value = RelocationType.COPY
    }

    fun onMoveBtnClick() {
        onRelocateRequest.value = RelocationType.MOVE
    }

    fun getOnRelocateRequest(): MutableLiveData<RelocationType> {
        return onRelocateRequest
    }

    fun onPublishBtnClick() {
    }

    fun onDeleteBtnClick() {
        onRecordDeleteRequest.value = onRecordDeleteRequest.value
    }

    fun onEditBtnClick() {
    }

    fun onShareBtnClick() {
    }
}