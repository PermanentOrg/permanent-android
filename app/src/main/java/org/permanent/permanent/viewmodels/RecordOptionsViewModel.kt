package org.permanent.permanent.viewmodels

import android.app.Application
import android.os.Build
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.AccessPermissionsManager
import org.permanent.permanent.PermissionsHelper
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordOption
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.ui.myFiles.RelocationType

class RecordOptionsViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val isBusy = MutableLiveData<Boolean>()
    private val recordName = MutableLiveData<String>()
    private val hiddenOptions =
        MutableLiveData(mutableListOf(RecordOption.PUBLISH, RecordOption.EDIT))
    private val onRequestWritePermission = SingleLiveEvent<Void>()
    private val onFileDownloadRequest = MutableLiveData<Void>()
    private val onRecordDeleteRequest = MutableLiveData<Void>()
    private val onRecordShareRequest = MutableLiveData<Void>()
    private val onRelocateRequest = MutableLiveData<RelocationType>()

    fun setRecord(record: Record?, isShownInMyFilesFragment: Boolean?) {
        recordName.value = record?.displayName
        if (record?.type == RecordType.FOLDER) {
            hiddenOptions.value?.add(RecordOption.DOWNLOAD)
        }
        if (isShownInMyFilesFragment == true) {
            if (!AccessPermissionsManager.instance.isCreateAvailable())
                hiddenOptions.value?.add(RecordOption.COPY)
            if (!AccessPermissionsManager.instance.isDeleteAvailable())
                hiddenOptions.value?.add(RecordOption.DELETE)
            if (!AccessPermissionsManager.instance.isMoveAvailable())
                hiddenOptions.value?.add(RecordOption.MOVE)
            if (!AccessPermissionsManager.instance.isShareAvailable())
                hiddenOptions.value?.add(RecordOption.SHARE)
        } else {
            hiddenOptions.value?.add(RecordOption.DELETE)
            hiddenOptions.value?.add(RecordOption.MOVE)
            hiddenOptions.value?.add(RecordOption.SHARE)
            hiddenOptions.value?.add(RecordOption.COPY)
        }
    }

    fun onDownloadBtnClick() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
            && !PermissionsHelper().hasWriteStoragePermission(appContext)
        ) {
            onRequestWritePermission.call()
        } else {
            startFileDownload()
        }
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

    fun onPublishBtnClick() {
    }

    fun onDeleteBtnClick() {
        onRecordDeleteRequest.value = onRecordDeleteRequest.value
    }

    fun onEditBtnClick() {
    }

    fun onShareBtnClick() {
        onRecordShareRequest.value = onRecordShareRequest.value
    }

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getName(): MutableLiveData<String> = recordName

    fun getHiddenOptions(): MutableLiveData<MutableList<RecordOption>> = hiddenOptions

    fun getOnRequestWritePermission(): MutableLiveData<Void> = onRequestWritePermission

    fun getOnFileDownloadRequest(): MutableLiveData<Void> = onFileDownloadRequest

    fun getOnRelocateRequest(): MutableLiveData<RelocationType> = onRelocateRequest

    fun getOnRecordDeleteRequest(): MutableLiveData<Void> = onRecordDeleteRequest

    fun getOnRecordShareRequest(): MutableLiveData<Void> = onRecordShareRequest
}
