package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository

class RenameRecordViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val currentRecordName = MutableLiveData<String>()
    private val nameError = MutableLiveData<Int>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onRecordRenamed = SingleLiveEvent<Void>()
    private val showMessage = SingleLiveEvent<String>()
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)

    fun setRecordName(displayName: String?) {
        currentRecordName.value = displayName
    }

    fun getCurrentRecordName(): MutableLiveData<String> {
        return currentRecordName
    }

    fun getNameError(): LiveData<Int> {
        return nameError
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getOnRecordRenamed(): MutableLiveData<Void> {
        return onRecordRenamed
    }

    fun getOnShowMessage(): LiveData<String> {
        return showMessage
    }

    fun onNameTextChanged(name: Editable) {
        currentRecordName.value = name.toString()
    }

    private fun getValidatedName(): String? {
        val name = currentRecordName.value?.trim()

        if (name.isNullOrEmpty()) {
            nameError.value = R.string.invalid_name_error
            return null
        }
        nameError.value = null
        return name
    }

    fun renameRecord(record: Record) {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        val recordName = getValidatedName()

        if (recordName != null) {
            isBusy.value = true
            fileRepository.updateRecord(record, recordName, object : IResponseListener {
                override fun onSuccess(message: String?) {
                    isBusy.value = false
                    showMessage.value = message
                    onRecordRenamed.call()
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    showMessage.value = error
                }
            })
        }
    }
}
