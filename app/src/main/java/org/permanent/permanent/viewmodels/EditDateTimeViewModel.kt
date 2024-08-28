package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository

class EditDateTimeViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)
    private var records: MutableList<Record> = mutableListOf()
    var shouldClose: MutableState<Boolean> = mutableStateOf(false)
    var isBusy: MutableState<Boolean> = mutableStateOf(false)
    val showMessage = mutableStateOf("")

    fun setRecords(records: ArrayList<Record>) {
        this.records.addAll(records)
    }

    fun updateDate(dateString: String) {
        this.records.forEach {
            it.displayDate = dateString
        }
        applyChanges()
    }

    private fun applyChanges() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        fileRepository.updateMultipleRecords(records = records,
            isFolderRecordType = false,
            object : IResponseListener {
                override fun onSuccess(message: String?) {
                    isBusy.value = false
                    shouldClose.value = true
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    error?.let {
                        showMessage.value = it
                    }
                }
            })
    }
}