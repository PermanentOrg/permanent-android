package org.permanent.permanent.viewmodels

import android.app.Application
import android.os.Build
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.PermissionsHelper
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository
import org.permanent.permanent.ui.myFiles.RelocationType

class RecordOptionsViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val isBusy = MutableLiveData<Boolean>()
    private val isFolder = MutableLiveData(false)
    private val recordName = MutableLiveData<String>()
    private val onRequestWritePermission = SingleLiveEvent<Void>()
    private val onFileDownloadRequest = MutableLiveData<Void>()
    private val onFileDeleteRequest = MutableLiveData<Void>()
    private val onRelocateRequest = MutableLiveData<RelocationType>()
    private val onRecordDeleted = SingleLiveEvent<Void>()
    private val onErrorMessage = MutableLiveData<String>()
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)

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
        return onFileDeleteRequest
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
        onFileDeleteRequest.value = onFileDeleteRequest.value
    }

    fun getOnRecordDeleted(): MutableLiveData<Void> {
        return onRecordDeleted
    }

    fun getOnErrorMessage(): MutableLiveData<String> {
        return onErrorMessage
    }

    fun onEditBtnClick() {
    }

    fun onShareBtnClick() {
    }

    fun delete(record: Record) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        fileRepository.deleteRecord(record, object : IFileRepository.IOnResponseListener {
            override fun onSuccess(message: String?) {
                isBusy.value = false
                onRecordDeleted.call()
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                onErrorMessage.value = error
            }
        })
    }
}