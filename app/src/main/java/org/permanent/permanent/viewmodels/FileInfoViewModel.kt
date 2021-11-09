package org.permanent.permanent.viewmodels

import android.app.Application
import android.app.DatePickerDialog
import android.text.Editable
import android.widget.DatePicker
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.CurrentArchivePermissionsManager
import org.permanent.permanent.R
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.models.FileData
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository

class FileInfoViewModel(
    application: Application
) : ObservableAndroidViewModel(application), DatePickerDialog.OnDateSetListener {

    private val appContext = application.applicationContext
    private lateinit var fileData: FileData
    private val name = MutableLiveData<String>()
    private val description = MutableLiveData<String>()
    private val date = MutableLiveData<String>()
    private val address = MutableLiveData<String>()
    private val onShowDatePickerRequest = SingleLiveEvent<Void>()
    val onShowLocationSearchRequest = SingleLiveEvent<Void>()
    val onShowTagsEdit = SingleLiveEvent<Void>()
    private val onFileInfoUpdated = SingleLiveEvent<String>()
    private val showMessage = SingleLiveEvent<String>()
    private val showError = SingleLiveEvent<String>()
    private val existsTags = MutableLiveData(false)
    private val isEditable = MutableLiveData(false)
    private val isBusy = MutableLiveData(false)
    private var fileRepository: IFileRepository = FileRepositoryImpl(application)

    fun setFileData(fileData: FileData) {
        this.fileData = fileData
        isEditable.value = !(!CurrentArchivePermissionsManager.instance.isEditAvailable()
                || fileData.accessRole == AccessRole.VIEWER
                || fileData.accessRole == AccessRole.CONTRIBUTOR)
        name.value = fileData.displayName
        description.value = fileData.description
        date.value = fileData.displayDate
        address.value = fileData.completeAddress
        existsTags.value = !fileData.tags.isNullOrEmpty()
    }

    fun onNameTextChanged(text: Editable) {
        name.value = text.toString()
    }

    fun onDescriptionTextChanged(text: Editable) {
        description.value = text.toString()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
        date.value = "$year-${month + 1}-$day"
        saveChanges()
    }

    fun onDateClick() {
        if (isEditable.value == true)
            onShowDatePickerRequest.value = onShowDatePickerRequest.value
    }

    fun onLocationClick() {
        if (isEditable.value == true)
            onShowLocationSearchRequest.value = onShowLocationSearchRequest.value
    }

    fun onEditTagsClick() {
        if (isEditable.value == true)
            onShowTagsEdit.call()
    }

    fun saveChanges() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        val nameValue = name.value?.trim()
        val description = description.value?.trim()
        val date = date.value

        if (nameValue.isNullOrEmpty()) {
            showError.value = appContext.getString(R.string.invalid_name_error)
            name.value = fileData.displayName
            return
        }

        if (fileData.displayName != nameValue
            || fileData.description != description
            || fileData.displayDate != date
        ) {
            fileData.displayName = nameValue
            fileData.description = description
            fileData.displayDate = date

            isBusy.value = true
            fileRepository.updateRecord(fileData, object : IResponseListener {
                override fun onSuccess(message: String?) {
                    isBusy.value = false
                    showMessage.value = message
                    onFileInfoUpdated.value = nameValue
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    showError.value = error
                }
            })
        }
    }

    fun getName(): LiveData<String> = name

    fun getDescription(): LiveData<String> = description

    fun getDate(): LiveData<String> = date

    fun getAddress(): LiveData<String> = address

    fun getShowDatePicker(): LiveData<Void> = onShowDatePickerRequest

    fun getShowLocationSearch(): LiveData<Void> = onShowLocationSearchRequest

    fun getOnFileInfoUpdated(): LiveData<String> = onFileInfoUpdated

    fun getShowTagsEdit(): LiveData<Void> = onShowTagsEdit

    fun getShowError(): LiveData<String> = showError

    fun getShowMessage(): LiveData<String> = showMessage

    fun getExistsTags(): LiveData<Boolean> = existsTags

    fun getIsEditable(): LiveData<Boolean> = isEditable

    fun getIsBusy(): LiveData<Boolean> = isBusy
}