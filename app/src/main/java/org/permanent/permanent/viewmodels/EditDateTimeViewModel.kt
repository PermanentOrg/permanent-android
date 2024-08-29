package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
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
    private val onDateChanged = MutableLiveData<String>()

    fun setRecords(records: ArrayList<Record>) {
        this.records.addAll(records)
    }

    fun updateDate(dateString: String) {
        this.records.forEach {
            it.displayDate = dateString
        }
        applyChanges(dateString)
    }

    private fun applyChanges(dateString: String) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        isBusy.value = true
        fileRepository.updateMultipleRecords(records = records,
            isFolderRecordType = false,
            object : IResponseListener {
                override fun onSuccess(message: String?) {
                    isBusy.value = false
                    shouldClose.value = true
                    onDateChanged.value = dateString
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    error?.let {
                        showMessage.value = it
                    }
                }
            })
    }

    fun getOnDateChanged() = onDateChanged
}